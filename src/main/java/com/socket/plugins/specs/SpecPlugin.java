package com.socket.plugins.specs;

import com.google.inject.Provides;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketReceivePacket;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "<html><font color=#b20e0e>[M] spec counter",
		description = "Track specs over socket",
		tags = {"combat", "npcs", "overlay"},
		enabledByDefault = true
)
public class SpecPlugin extends Plugin
{
	private int currentWorld = -1;
	private int specialPercentage = -1;
	private int specialHitpointsExperience = -1;
	private int specialHitpointsGained = -1;
	private boolean specialUsed = false;
	private double modifier = 2d;

	private SpecialWeapon specialWeapon;
	private final Set<Integer> interactedNpcIds = new HashSet<>();
	private final SpecialCounter[] specialCounter = new SpecialCounter[SpecialWeapon.values().length];

	private NPC lastTarget;
	private int lastSpecTick;
//	private long specialExperience = -1L;
//	private long magicExperience = -1L;
	private int minSpecHit;

	private String identifier = "special-extended";

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private WSClient wsClient;
	@Inject
	private PartyService party;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private EventBus eventBus;
	@Inject
	private SpecialHitOverlay overlay;
	@Inject
	private SpecConfig config;

	@Provides
    SpecConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpecConfig.class);
	}

	@Override
	protected void startUp()
	{
		reset();
		wsClient.registerMessage(PartySpecUpdate.class);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		reset();
		wsClient.unregisterMessage(PartySpecUpdate.class);
		overlayManager.remove(overlay);
		removeCounters();
	}

	protected void reset(){
		currentWorld = -1;
		specialPercentage = -1;
		lastTarget = null;
		lastSpecTick = -1;
		interactedNpcIds.clear();
		specialUsed = false;
//		specialExperience = -1L;
//		magicExperience = -1L;
		minSpecHit = -1;
		removeCounters();
//		identifier = config.identifier() ? "special-extended" : "m-specs";
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (currentWorld == -1)
			{
				currentWorld = client.getWorld();
			}
			else if (currentWorld != client.getWorld())
			{
				currentWorld = client.getWorld();
				removeCounters();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event){
		if (event.getGroup().equals("mspeccounter") && event.getKey().equals("identifier")) {
			identifier = config.identifier() ? "special-extended" : "m-specs";
			// clear();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged) {
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();
		if (source != client.getLocalPlayer() || target == null) {
			return;
		}
		lastTarget = (NPC) target;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int specialPercentage = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage) {
			this.specialPercentage = specialPercentage;
			return;
		}

		this.specialPercentage = specialPercentage;
		specialWeapon = usedSpecialWeapon();

		debug("spec used ---------------------");

		if (client.getLocalPlayer() != null) {
			debug("specUsed has been set");
			lastSpecTick = client.getTickCount();
			specialHitpointsExperience = client.getSkillExperience(Skill.HITPOINTS);
			specialHitpointsGained = -1;
//		specialExperience = this.client.getOverallExperience();
//		magicExperience = this.client.getSkillExperience(Skill.MAGIC);

			specialUsed = true;
			minSpecHit = -1;
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event) {
		if (event.getActor().getName() != null && event.getActor().getGraphic() == 169) {
			updateCounter("", SpecialWeapon.VULNERABILITY,0);
			socketSend("",((NPC) event.getActor()).getId(), SpecialWeapon.VULNERABILITY, 0);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (specialUsed && statChanged.getSkill() == Skill.HITPOINTS)
		{
			specialHitpointsGained = statChanged.getXp() - specialHitpointsExperience;
		}
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop fakeXpDrop)
	{
		if (specialUsed && fakeXpDrop.getSkill() == Skill.HITPOINTS)
		{
			specialHitpointsGained = fakeXpDrop.getXp();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;

		if (specialUsed) {
			debug("ongametick: spec used " + lastTarget.getName());
		}

		if (lastSpecTick != -1 && lastSpecTick < client.getTickCount() - 6){
			// reset: last spec occurred too long ago
			clear();
			return;
		}

		if (specialUsed && lastTarget != null)
		{
			System.out.println("spec used and last spec target != null");


			int deltaExperience = specialHitpointsGained;  // TODO: check how this works with fake xp drops

			if (deltaExperience > 0)
			{
				if (specialWeapon != null) {
					minSpecHit = getHitFromXp(specialWeapon, deltaExperience);  // returns 1 if non dmg based spec

					if (config.guessDawnbringer() && specialWeapon == SpecialWeapon.DAWNBRINGER) {
						minSpecHit *= 1.9;
					}
					else if (specialWeapon.isDamage()){
						return; // go to onHitsplat
					}

					debug("xp drop spec detected");

					if (!interactedNpcIds.contains(lastTarget.getId())) {
						removeCounters();
						addInteracting(lastTarget.getId());
					}

					String pName = client.getLocalPlayer().getName();
					updateCounter(pName, specialWeapon, minSpecHit);

					if (!party.getMembers().isEmpty())
					{
						final PartySpecUpdate partySpecUpdate = new PartySpecUpdate(lastTarget.getId(), specialWeapon, minSpecHit);
						partySpecUpdate.setMemberId(party.getLocalMember().getMemberId());
						wsClient.send(partySpecUpdate);
					}
					socketSend(pName, lastTarget.getId(), specialWeapon, minSpecHit);

					clear();
				}

			}
			else { // no exp gained: hit splashed
				clear();
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		if (!specialUsed || minSpecHit == -1) return; // means either no spec or no hp exp was gained so spec missed
		// is this necessary? already gets reset in onGameTick. maybe depends on order things are processed

		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
//		Hitsplat.HitsplatType hitsplatType = hitsplat.getHitsplatType();
		if (!hitsplat.isMine() || target == client.getLocalPlayer() || hitsplat.getAmount() < minSpecHit || hitsplat.getAmount() > minSpecHit * 2.5) {
			// hitsplat is not mine or hitsplat was on me or hitsplat is too big/small to be the spec
			return;
		}
		if (lastTarget != null && lastTarget != target) {
			debug("hitsplat code stopped because wrong target");
			return;
		}

//		specialExperience = -1L;
//		magicExperience = -1L;
		if (!(target instanceof NPC)) {
			return;
		}
		NPC npc = (NPC)target;
		int interactingId = npc.getId();
		if (!interactedNpcIds.contains(interactingId)) {
			removeCounters();
			addInteracting(interactingId);
		}
		if (specialWeapon != null) {
			if (!specialWeapon.isDamage()){
				log.debug("non dmg based spec weapon processed hitsplat: check.");
			}
			int hit = getHit(specialWeapon, hitsplat);
			log.debug("Special attack target: id: {} - target: {} - weapon: {} - amount: {}", interactingId, target.getName(), specialWeapon, hit);
			String pName = Objects.requireNonNull(client.getLocalPlayer()).getName();
			updateCounter(pName, specialWeapon, hit);

			if (!party.getMembers().isEmpty())
			{
				final PartySpecUpdate partySpecUpdate = new PartySpecUpdate(interactingId, specialWeapon, minSpecHit);
				partySpecUpdate.setMemberId(party.getLocalMember().getMemberId());
				wsClient.send(partySpecUpdate);
			}

			socketSend(pName, interactingId, specialWeapon, hit);
			clear();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (isSotetseg(npcDespawned.getNpc().getId()) && client.getLocalPlayer().getWorldLocation().getPlane() == 3) {
			return;
		}
		// TODO: do something similar with other multi form bosses

		NPC actor = npcDespawned.getNpc();
		if (lastTarget == actor) {
			lastTarget = null;
		}
		if (actor.isDead() && interactedNpcIds.contains(actor.getId())) {
			removeCounters();
			JSONObject payload = new JSONObject();
			payload.put(identifier+"-bossdead", "dead");
			eventBus.post(new SocketBroadcastPacket(payload));
		}
	}

	private void socketSend(String playerName, int interactingId, SpecialWeapon specialWeapon, int hit){
		debug("sending socket thing --------------------------------------");
		JSONObject data = new JSONObject();
		data.put("player", playerName);
		data.put("target", interactingId);
		data.put("weapon", specialWeapon.ordinal());
		data.put("hit", hit);

		JSONObject payload = new JSONObject();
		payload.put(identifier, data);
		eventBus.post(new SocketBroadcastPacket(payload));
	}

	private int checkInteracting()  // is this better than last target? probably, since multi form bosses are included
	{
		Player localPlayer = client.getLocalPlayer();
		Actor interacting = localPlayer.getInteracting();

		if (interacting instanceof NPC)
		{
			NPC npc = (NPC) interacting;
			NPCComposition composition = npc.getComposition();
			int interactingId = npc.getId();

			if (!ArrayUtils.contains(composition.getActions(), "Attack"))
			{
				// Skip over non attackable npcs so that eg. talking to bankers doesn't reset
				// the counters.
				return -1;
			}

			if (!interactedNpcIds.contains(interactingId))
			{
				removeCounters();
				addInteracting(interactingId);
			}

			return interactingId;
		}

		return -1;
	}

	private void clear(){
		debug("clearing spec");
		specialHitpointsGained = 0;
		lastTarget = null;
		specialUsed = false;
		minSpecHit = -1;
		lastSpecTick = -1;
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event) {
		try {
			if (client.getGameState() != GameState.LOGGED_IN) {
				return;
			}
			debug("socket received packed");
			JSONObject payload = event.getPayload();
			if (payload.has(identifier)) {
				debug("received socket thing, identifier "+ identifier);
				String pName = client.getLocalPlayer().getName();
				JSONObject data = payload.getJSONObject(identifier);
				if (data.getString("player").equals(pName)) { // ignore own packets
					return;
				}
				clientThread.invoke(() -> {
					SpecialWeapon weapon = SpecialWeapon.values()[data.getInt("weapon")];
					String attacker = data.getString("player");
					int targetId = data.getInt("target");
					if (!interactedNpcIds.contains(targetId)) {
						removeCounters();
						addInteracting(targetId);
					}
					updateCounter(attacker, weapon, data.getInt("hit"));
				});
			} else if (payload.has(identifier+"-bossdead")) {
				removeCounters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addInteracting(int npcId)
	{
		interactedNpcIds.add(npcId);

		// Add alternate forms of bosses
		final Boss boss = Boss.getBoss(npcId);
		if (boss != null)
		{
//				modifier = boss.getModifier(); // TODO: readd multipliers to make min dmg threshold more accurate?
			interactedNpcIds.addAll(boss.getIds());
		}
	}

	private int getHit(SpecialWeapon specialWeapon, Hitsplat hitsplat) {
		return specialWeapon.isDamage() ? hitsplat.getAmount() : 1;
	}

	private int getHitFromXp(SpecialWeapon specialWeapon, int deltaHpExp)
	{
		double modifierBase = 1/modifier;
		double damageOutput = (deltaHpExp * modifierBase) / 1.3333d;

		return specialWeapon.isDamage()? (int) Math.ceil(damageOutput) : 1;
	}

	// disc party plugin
	@Subscribe
	public void onPartySpecUpdate(PartySpecUpdate event)
	{
		if (party.getLocalMember().getMemberId().equals(event.getMemberId()))
		{
			return;
		}

		String name = party.getMemberById(event.getMemberId()).getName();
		if (name == null)
		{
			return;
		}

		clientThread.invoke(() ->
		{
			// If not interacting with any npcs currently, add to interacting list
			if (interactedNpcIds.isEmpty())
			{
				addInteracting(event.getNpcId());
			}

			// Otherwise we only add the count if it is against a npc we are already tracking
			if (interactedNpcIds.contains(event.getNpcId()))
			{
				updateCounter(name, event.getWeapon(), event.getHit());
			}
		});
	}

	private SpecialWeapon usedSpecialWeapon() {
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null) {
			return null;
		}
		Item[] items = equipment.getItems();
		int weaponIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
		if (weaponIdx >= items.length) {
			return null;
		}
		Item weapon = items[weaponIdx];
		for (SpecialWeapon specialWeapon : SpecialWeapon.values()) {
			if (specialWeapon.getItemID() == weapon.getId()) {
				return specialWeapon;
			}
		}
		return null;
	}

	private void updateCounter(String player, SpecialWeapon specialWeapon, int hit) {
		debug("update counter");

		if (specialWeapon == SpecialWeapon.BANDOS_GODSWORD_OR) {
			specialWeapon = SpecialWeapon.BANDOS_GODSWORD;
		}
		else if (specialWeapon == SpecialWeapon.DARKLIGHT) {
			specialWeapon = SpecialWeapon.ARCLIGHT;
		}
		else if (specialWeapon == SpecialWeapon.BONE_DAGGER_P || specialWeapon == SpecialWeapon.BONE_DAGGER_PP || specialWeapon == SpecialWeapon.BONE_DAGGER_S){
			specialWeapon = SpecialWeapon.BONE_DAGGER;
		}
		SpecialCounter counter = specialCounter[specialWeapon.ordinal()];
		BufferedImage image;
		if (specialWeapon == SpecialWeapon.VULNERABILITY){
			IndexDataBase sprite = client.getIndexSprites();
			image = Objects.requireNonNull(client.getSprites(sprite, 56, 0))[0].toBufferedImage();
		}
		else {
			 image = itemManager.getImage(specialWeapon.getItemID());
		}
		overlay.addOverlay(player, new SpecialIcon(image, Integer.toString(hit), System.currentTimeMillis()));

		if (counter == null) {
			counter = new SpecialCounter(image, this, hit, specialWeapon);
			infoBoxManager.addInfoBox(counter);
			specialCounter[specialWeapon.ordinal()] = counter;
		}
		else {
			counter.addHits(hit);
		}
		// If in a party, add hit to partySpecs for the infobox tooltip. TODO: do this for vuln?
		Map<String, Integer> partySpecs = counter.getPartySpecs();
		if (!party.getMembers().isEmpty()) {
			if (partySpecs.containsKey(player)) {
				partySpecs.put(player, hit + partySpecs.get(player));
			}
			else {
				partySpecs.put(player, hit);
			}

		}
	}

	private void removeCounters() {
		interactedNpcIds.clear();
		for (int i = 0; i < specialCounter.length; ++i){
			SpecialCounter counter = specialCounter[i];
			if (counter != null){
				infoBoxManager.removeInfoBox(counter);
				specialCounter[i] = null;
			}
		}
	}

	private void debug(String s, Object ...a){
		if (config.debug()){
			System.out.println(s);
//			log.debug(s, a);
		}
	}

	public boolean isSotetseg(int id) {
		return Boss.getBoss(id) == Boss.SOTETSEG;
	}
}