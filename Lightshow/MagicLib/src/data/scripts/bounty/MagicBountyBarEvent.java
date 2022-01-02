package data.scripts.bounty;

import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
//import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.MagicPaginatedBarEvent;
import data.scripts.util.MagicSettings;
import data.scripts.util.MagicVariables;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.fs.starfarer.api.util.Misc.random;
import static data.scripts.util.MagicCampaign.RelativeEffectiveStrength;
import static data.scripts.util.MagicTxt.getString;
import static data.scripts.util.MagicTxt.nullStringIfEmpty;
import static data.scripts.util.MagicVariables.MAGICLIB_ID;

/**
 * Displays the bounty board and all associated bounties.
 *
 * @author Wisp, Tartiflette
 */
public final class MagicBountyBarEvent extends MagicPaginatedBarEvent {
    private List<String> keysOfBountiesToShow;
    private MarketAPI market;

    /**
     * This method is not called, as the Bar Event is triggered directly in ShowMagicBountyBoardCmd.
     */
    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return MagicBountyCoordinator.getInstance().shouldShowBountyBoardAt(market);
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Display the text that will appear when the player first enters the bar and looks around
        //"A subroutine from your implant informs you that this establishment is broadcasting an informal job board."
        dialog.getTextPanel().addPara(getString("mb_greeting"));

        // Display the option that lets the player choose to investigate our bar event
        //"Connect to the local unsanctioned bounty board."
        dialog.getOptionPanel().addOption(getString("mb_connect"), this);
    }

    /**
     * Called when the player chooses this event from the list of options shown when they enter the bar.
     */
    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        this.market = dialog.getInteractionTarget().getMarket();

        // If player starts our event, then backs out of it, `done` will be set to true.
        // If they then start the event again without leaving the bar, we should reset `done` to false.        
        done = false;
        // Clear on init in case it's being reopened
        options.clear();
        optionsAllPages.clear();

        dialog.getVisualPanel().saveCurrentVisual();

        // The boolean is for whether to show only minimal person information. True == minimal
//        dialog.getVisualPanel().showPersonInfo(person, true);

        // Launch into our event by triggering the "INIT" option, which will call `optionSelected()`
        this.optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        super.optionSelected(optionText, optionData);
        String acceptJobKeyPrefix = "accept--";
        String dismissJobKeyPrefix = "dismiss--";
        String confirmDismissJobKeyPrefix = "confirm-dismiss--";

        MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
        if (optionData instanceof OptionId) {
            TextPanelAPI text = dialog.getTextPanel();

            OptionId optionId = (OptionId) optionData;
            switch (optionId) {
                case INIT:
                case BACK_TO_BOARD:
                    showBountyBoard(bountyCoordinator, text);
                    break;
                case CLOSE:
                    noContinue = true;
                    done = true;
                    break;
                default:
            }
        } else if (optionData instanceof String) {
            String data = (String) optionData;

            if (data.startsWith(acceptJobKeyPrefix)) {
                // Player accepted a bounty.
                try {
                    String bountyKey = data.replaceFirst(acceptJobKeyPrefix, "");
                    MagicBountyData.bountyData bounty = MagicBountyData
                            .getBountyData(bountyKey);
                    //"Accepted job: "
                    text.addPara("%s", Misc.getHighlightColor(), getString("mb_accepted") + bounty.job_name);

                    ActiveBounty activeBounty = bountyCoordinator.getActiveBounty(bountyKey);
                    activeBounty.acceptBounty(dialog.getInteractionTarget(), activeBounty.calculateCreditReward(), (float) bounty.job_reputation_reward, bounty.job_forFaction);
                    removeBountyFromBoard(bountyKey);

                    optionSelected(null, OptionId.BACK_TO_BOARD);
                } catch (Exception e) {
                    Global.getLogger(this.getClass()).error(e.getMessage(), e);
                }
            } else if (data.startsWith(dismissJobKeyPrefix)) {
                // Player chose to dismiss a bounty (but needs to confirm the dismissal).
                try {
                    String bountyKey = data.replaceFirst(dismissJobKeyPrefix, "");
                    text.addPara("%s", Misc.getHighlightColor(), getString("mb_permDismissConfirm"));
                    options.clear();
                    optionsAllPages.clear();
                    addOption(getString("mb_permDismissConfirmOpt"), confirmDismissJobKeyPrefix + bountyKey, null, null);
                    addOption(getString("mb_returnBounty"), getBountyDetailsOptionKey(bountyKey), null, null);
                    addOption(getString("mb_returnBoard"), OptionId.BACK_TO_BOARD, null, Keyboard.KEY_ESCAPE);
                } catch (Exception e) {
                    Global.getLogger(this.getClass()).error(e.getMessage(), e);
                }
            } else if (data.startsWith(confirmDismissJobKeyPrefix)) {
                // Player has confirmed they want to permanently dismiss a bounty.
                try {
                    String bountyKey = data.replaceFirst(confirmDismissJobKeyPrefix, "");
                    MagicBountyCoordinator.getInstance().getActiveBounty(bountyKey).endBounty(new ActiveBounty.BountyResult.DismissedPermanently());
                    removeBountyFromBoard(bountyKey);
                    text.addPara("%s", Misc.getHighlightColor(), getString("mb_permDismissConfirmed"));
                    options.clear();
                    optionsAllPages.clear();
                    optionSelected(null, OptionId.BACK_TO_BOARD);
                } catch (Exception e) {
                    Global.getLogger(this.getClass()).error(e.getMessage(), e);
                }
            } else {
                for (String key : keysOfBountiesToShow) {
                    if (getBountyDescriptionOptionKey(key).equals(optionData) || getBountyDetailsOptionKey(key).equals(optionData)) {
                        // Player has selected to view the description or details of a bounty
                        final MagicBountyData.bountyData bounty = MagicBountyData.getBountyData(key);

                        if (bounty == null)
                            continue;

                        ActiveBounty activeBounty = bountyCoordinator.getActiveBounty(key);

                        if (activeBounty == null) {
                            activeBounty = bountyCoordinator.createActiveBounty(key, bounty);

                            if (activeBounty == null) continue;
                        }

                        options.clear();
                        optionsAllPages.clear();

                        if (getBountyDescriptionOptionKey(key).equals(optionData)) {
                            // Player has selected to view a bounty

                            //FLAVOR TEXT
                            activeBounty.addDescriptionToTextPanel(text);
                            
                            //illustration panel
                            if (bounty.job_show_captain) {
                                //diplaying the captain takes priority
                                dialog.getVisualPanel().showPersonInfo(activeBounty.getFleet().getCommander());
//                            } else if(bounty.job_show_fleet != MagicBountyData.ShowFleet.None && bounty.job_show_fleet != MagicBountyData.ShowFleet.Text){
//                                //displaying the flagship comes in second if the fleet is visible
//                                dialog.getVisualPanel().showFleetMemberInfo(activeBounty.getFleet().getFlagship());
                            } else {
                                //otherwise, just keep the board illustration
                                dialog.getVisualPanel().showImagePortion("intel", "magicBoard", 128, 128, 0, 0, 256, 256);
                            }
                            
                            //TEST TO ADD LOCATION MAP
                            if (bounty.job_show_distance== MagicBountyData.ShowDistance.Exact){
                                dialog.getVisualPanel().showMapMarker(
                                        activeBounty.getFleetSpawnLocation(), 
					null,
                                        null, 
					false,
                                        null,
                                        null,
                                        null
                                );
                            } 
                            if (bounty.job_show_distance== MagicBountyData.ShowDistance.Vanilla
                                    ||bounty.job_show_distance== MagicBountyData.ShowDistance.VanillaDistance ){
                                dialog.getVisualPanel().showMapMarker(
                                        activeBounty.getFleetSpawnLocation().getStarSystem().getHyperspaceAnchor(), 
					null,
                                        null, 
					false,
                                        null,
                                        null,
                                        null
                                );
                            }
                            
                            addOption(getString("mb_continue"), getBountyDetailsOptionKey(key), null, null);
                            addOption(getString("mb_returnBoard"), OptionId.BACK_TO_BOARD, null, Keyboard.KEY_ESCAPE);
                        } else if (getBountyDetailsOptionKey(key).equals(optionData)) {

                            //HVB STYLE TARGET DESCRIPTION
                            if (bounty.job_memKey.startsWith("$HVB_")) {
                                //adding HVB descriptions
                                MagicBountyHVB.generateFancyFleetDescription(text, activeBounty.getFleet(), activeBounty.getCaptain());
                                MagicBountyHVB.generateFancyCommanderDescription(text, activeBounty.getFleet(), activeBounty.getCaptain());
                            }

                            //OFFERING FACTION

                            if (bounty.job_show_captain == false && bounty.job_show_fleet == MagicBountyData.ShowFleet.None) {
                                //"Posted by %s."
                                if (bounty.job_forFaction != null) {
                                    FactionAPI faction = Global.getSector().getFaction(bounty.job_forFaction);
                                    text.addPara(getString("mb_from"), faction.getBaseUIColor(), faction.getDisplayNameWithArticle());
                                } else {
                                    text.addPara(getString("mb_from"), Misc.getHighlightColor(), getString("mb_unknown"));
                                }
                            } else {
                                //"Posted by %s, against %s."
                                if (bounty.job_forFaction != null) {
                                    FactionAPI faction = Global.getSector().getFaction(bounty.job_forFaction);
                                    FactionAPI target = activeBounty.getFleet().getFaction();
                                    text.addPara(getString("mb_fromAgainst"), Misc.getHighlightColor(), faction.getDisplayNameWithArticle(), target.getDisplayNameWithArticle());
                                    text.setHighlightColorsInLastPara(faction.getBaseUIColor(), target.getBaseUIColor());
                                } else {
                                    FactionAPI target = activeBounty.getFleet().getFaction();
                                    text.addPara(getString("mb_fromAgainst"), Misc.getHighlightColor(), getString("mb_unknown"), target.getDisplayNameWithArticle());
                                    text.setHighlightColorsInLastPara(Misc.getHighlightColor(), target.getBaseUIColor());
                                }
                            }

                            //REWARD
                            Float creditReward = activeBounty.calculateCreditReward();
                            if (creditReward != null) {
                                //"Reward: %s"
                                text.addPara(getString("mb_credits"), Misc.getHighlightColor(), Misc.getDGSCredits(creditReward));
                            }

                            //DEADLINE
                            if (bounty.job_deadline > 0) {
                                //"Time limit: %s days"
                                text.addPara(getString("mb_time"), Misc.getHighlightColor(), Misc.getWithDGS(bounty.job_deadline));
                            }

                            //DISTANCE
                            if (bounty.job_show_distance != MagicBountyData.ShowDistance.None) {
                                switch (bounty.job_show_distance) {
                                    case Vague:
                                        float distance = activeBounty.getFleetSpawnLocation().getContainingLocation().getLocation().length();
                                        String vague = getString("mb_distance_core");
                                        if (distance > MagicVariables.getSectorSize() * 0.66f) {
                                            vague = getString("mb_distance_far");
                                        } else if (distance > MagicVariables.getSectorSize() * 0.33f) {
                                            vague = getString("mb_distance_close");
                                        }
                                        text.addPara(getString("mb_distance_vague"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                vague);
                                        break;

                                    case Distance:
                                        text.addPara(getString("mb_distance"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                Math.round(Misc.getDistanceLY(market.getPrimaryEntity(), activeBounty.getFleetSpawnLocation())) + "");
                                        break;

                                    case Vanilla:
                                        text.addPara(MagicBountyUtils.createLocationEstimateText(activeBounty));
                                        break;

                                    case VanillaDistance:
                                        text.addPara(MagicBountyUtils.createLocationEstimateText(activeBounty) + " " + getString("mb_distance"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                Math.round(Misc.getDistanceLY(market.getPrimaryEntity(), activeBounty.getFleetSpawnLocation())) + "");
                                        break;

                                    case Exact:
                                        text.addPara(MagicBountyUtils.createLocationPreciseText(activeBounty));
                                        text.highlightLastInLastPara(activeBounty.getFleetSpawnLocation().getStarSystem().getNameWithLowercaseType(), Misc.getHighlightColor());
                                        break;

                                    default:
                                        break;
                                }
                            }

                            //MISSION TYPE
                            //This is an %s mission, to get the reward you will need to %s.
                            if (bounty.job_show_type) {
                                switch (bounty.job_type) {
                                    case Assassination:
                                        text.addPara(getString("mb_type"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                getString("mb_type_assassination1"), getString("mb_type_assassination2")
                                        );
                                        break;
                                    case Destruction:
                                        text.addPara(getString("mb_type"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                getString("mb_type_destruction1"), getString("mb_type_destruction2")
                                        );
                                        break;
                                    case Obliteration:
                                        text.addPara(getString("mb_type"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                getString("mb_type_obliteration1"), getString("mb_type_obliteration2")
                                        );
                                        break;
                                    case Neutralisation:
                                        text.addPara(getString("mb_type"),
                                                Misc.getTextColor(),
                                                Misc.getHighlightColor(),
                                                getString("mb_type_neutralisation1"), getString("mb_type_neutralisation2") + Math.round(100 * MagicSettings.getFloat(MAGICLIB_ID, "bounty_neutralisationThreshold")) + getString("mb_type_neutralisation3")
                                        );
                                        break;
                                }
                            }

                            //TARGET CAPTAIN
                            if(bounty.job_show_fleet != MagicBountyData.ShowFleet.None && bounty.job_show_fleet != MagicBountyData.ShowFleet.Text){
                                //displaying the flagship takes priority if possible
                                dialog.getVisualPanel().showFleetMemberInfo(activeBounty.getFleet().getFlagship());
                            } else
                            if (bounty.job_show_captain) {
                                //diplaying the captain is now second
                                dialog.getVisualPanel().showPersonInfo(activeBounty.getFleet().getCommander());
                            } else {
                                //otherwise, just keep the board illustration
                                dialog.getVisualPanel().showImagePortion("intel", "magicBoard", 128, 128, 0, 0, 256, 256);
                            }
                            
                            /*
                            OLD DISPLAY
                            
                            if (bounty.job_show_captain) {
                                dialog.getVisualPanel().showPersonInfo(activeBounty.getFleet().getCommander());
                            } else if (nullStringIfEmpty(bounty.job_forFaction) != null && activeBounty.getGivingFaction() != null) {
                                String factionLogoSpriteName = activeBounty.getGivingFaction().getLogo();
                                SpriteAPI sprite = Global.getSettings().getSprite(factionLogoSpriteName);
                                InteractionDialogImageVisual visual = new InteractionDialogImageVisual(factionLogoSpriteName, sprite.getWidth(), sprite.getHeight());
                                visual.setShowRandomSubImage(false);
                                dialog.getVisualPanel().showImageVisual(visual);
                            } else {
//                                InteractionDialogImageVisual visual = new InteractionDialogImageVisual("graphics/magic/icons/ml_bountyBoard.png", 128, 128);
//                                visual.setShowRandomSubImage(false);
//                                dialog.getVisualPanel().showImageVisual(visual);
                                dialog.getVisualPanel().showImagePortion("intel", "magicBoard", 128, 128, 0, 0, 256, 256);
                            }
                            */

                            //DIFFICULTY
                            if (
                                    bounty.job_difficultyDescription != null 
                                    && bounty.job_difficultyDescription.equals(getString("mb_threatAssesmentAuto"))
                                    ){
                                /*
                                int playerFleetStrength = Math.round(Global.getSector().getPlayerFleet().getEffectiveStrength());
                                float bountyFleetStrength = activeBounty.getFleet().getEffectiveStrength();
                                
                                String dangerStringPhrase;
                                                                                    //"an extreme danger"
                                       if (playerFleetStrength < bountyFleetStrength * 0.70f) {
                                    dangerStringPhrase = getString("mb_threatLevel6");
                                                                                    //"a deadly peril"
                                } else if (playerFleetStrength < bountyFleetStrength * 0.85f) {
                                    dangerStringPhrase = getString("mb_threatLevel5"); 
                                                                                    //"a significant challenge"
                                } else if (playerFleetStrength < bountyFleetStrength * 1.00f) {
                                    dangerStringPhrase = getString("mb_threatLevel4"); 
                                                                                    //"a moderate hazard"
                                } else if (playerFleetStrength < bountyFleetStrength * 1.15f) {
                                    dangerStringPhrase = getString("mb_threatLevel3"); 
                                                                                    //"a minor threat"
                                } else if (playerFleetStrength < bountyFleetStrength * 1.3f) {
                                    dangerStringPhrase = getString("mb_threatLevel2"); 
                                                                                    //"a negligible inconvenience" 
                                } else if (playerFleetStrength < bountyFleetStrength * 1.5f) {
                                    dangerStringPhrase = getString("mb_threatLevel1"); 
                                                                                    //"no risk whatsoever" 
                                } else {
                                    dangerStringPhrase = getString("mb_threatLevel0"); 
                                }
                                */     
                                
                                float threatLevel = RelativeEffectiveStrength(activeBounty.getFleet());       
                                       
                                String dangerStringPhrase;
                                                                                    //"an extreme danger"
                                       if (threatLevel < 0.5f) {
                                    dangerStringPhrase = getString("mb_threatLevel6");
                                                                                    //"a deadly peril"
                                } else if (threatLevel < 0.70f) {
                                    dangerStringPhrase = getString("mb_threatLevel5"); 
                                                                                    //"a significant challenge"
                                } else if (threatLevel < 1.85f) {
                                    dangerStringPhrase = getString("mb_threatLevel4"); 
                                                                                    //"a moderate hazard"
                                } else if (threatLevel < 1.00f) {
                                    dangerStringPhrase = getString("mb_threatLevel3"); 
                                                                                    //"a minor threat"
                                } else if (threatLevel < 1.15f) {
                                    dangerStringPhrase = getString("mb_threatLevel2"); 
                                                                                    //"a negligible inconvenience" 
                                } else if (threatLevel < 1.3f) {
                                    dangerStringPhrase = getString("mb_threatLevel1"); 
                                                                                    //"no risk whatsoever" 
                                } else {
                                    dangerStringPhrase = getString("mb_threatLevel0"); 
                                }      
                                 
                                //"Your intelligence officer informs you that the target poses "
                                text.addPara(getString("mb_threat1") + getString("mb_threat2"), Misc.getHighlightColor(), dangerStringPhrase);
                            } else if (nullStringIfEmpty(bounty.job_difficultyDescription) != null
                                    && !bounty.job_difficultyDescription.equals(getString("mb_threatAssesmentNone"))) {
                                text.addPara(bounty.job_difficultyDescription);
                            }

                            //SHOW FLEET
                            if (bounty.job_show_fleet != MagicBountyData.ShowFleet.None) {
                                showFleet(
                                        text,
                                        dialog.getTextWidth(),
                                        activeBounty.getFleet().getFaction().getBaseUIColor(),
                                        activeBounty.getSpec().job_show_fleet,
                                        activeBounty.getFleet().getFleetData().getMembersInPriorityOrder(),
                                        activeBounty.getFlagshipInFleet(),
                                        activeBounty.getPresetShipsInFleet()
                                );
                            }
                            
                            options.clear();
                            optionsAllPages.clear();
                            addOption(bounty.job_pick_option != null && !bounty.job_pick_option.isEmpty()
                                    ? bounty.job_pick_option
                                    : getString("mb_accept"), acceptJobKeyPrefix + key, null, null);
                            addOption(getString("mb_permDismissOpt"), dismissJobKeyPrefix + key, null, null);
                            addOption(getString("mb_return"), OptionId.BACK_TO_BOARD, null, Keyboard.KEY_ESCAPE);
                        }
                    }
                }
            }
        }

        showOptions();
    }

    private void showBountyBoard(MagicBountyCoordinator instance, TextPanelAPI text) {
        options.clear();
        
//        InteractionDialogImageVisual visual = new InteractionDialogImageVisual("graphics/magic/icons/ml_bountyBoard.png", 128, 128);
//        visual.setShowRandomSubImage(false);
//        dialog.getVisualPanel().showImageVisual(visual);
        dialog.getVisualPanel().showImagePortion("intel", "magicBoard", 128, 128, 0, 0, 256, 256);
        
//        dialog.getVisualPanel().saveCurrentVisual();                
        refreshBounties(market);

        //"jobs are available on the bounty board."
        text.addPara("%s " + (keysOfBountiesToShow.size() == 1 ? getString("mb_job") : getString("mb_jobs")) + getString("mb_available"),
                Misc.getHighlightColor(),
                Integer.toString(keysOfBountiesToShow.size()));
        if (Global.getSettings().isDevMode()) {
            if (!instance.shouldShowBountyBoardAt(market)) {
                text.addPara("[Dev mode: Bounty board would not have been displayed normally]",
                        Misc.getHighlightColor(),
                        Misc.getHighlightColor());
            }

            text.addPara(String.format("[Dev mode: Based on market size (%s), there are %s bounty slots available (%s slots on cooldown from being used)]",
                            market.getSize(),
                            getNumberOfBountySlots(market),
                            instance.getBountiesAcceptedAtMarket(market) != null
                                    ? instance.getBountiesAcceptedAtMarket(market).size()
                                    : 0
                    ),
                    Misc.getHighlightColor(),
                    Misc.getHighlightColor());
        }

        for (String key : keysOfBountiesToShow) {
            MagicBountyData.bountyData bounty = MagicBountyData.getBountyData(key);

            if (bounty != null) {
                addOption(bounty.job_name, getBountyDescriptionOptionKey(key), null, null);
            }
        }
        //"Close the board."
        addOptionAllPages(getString("mb_close"), OptionId.CLOSE, getString("mb_closed"), Keyboard.KEY_ESCAPE);
    }

    private void removeBountyFromBoard(String bountyKey) {
        keysOfBountiesToShow.remove(bountyKey);
        MagicBountyCoordinator.getInstance().setBlockBountyAtMarket(market, bountyKey);
    }

    enum OptionId {
        INIT,
        CLOSE,
        BACK_TO_BOARD
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }

    private void refreshBounties(@NotNull MarketAPI market) {
        MagicBountyCoordinator instance = MagicBountyCoordinator.getInstance();
        List<String> keysToReturn = new ArrayList<>();

        List<String> bountiesAcceptedAtMarket = instance.getBountiesAcceptedAtMarket(market);
        int numberOfBountySlots = Math.max(0, getNumberOfBountySlots(market) - (bountiesAcceptedAtMarket != null
                ? bountiesAcceptedAtMarket.size()
                : 0));

        Map<String, MagicBountyData.bountyData> bountiesAtMarketById = instance.getBountiesWithChanceToSpawnAtMarketById(market);
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(new Random(instance.getMarketBountyBoardGenSeed(market)));

        for (Map.Entry<String, MagicBountyData.bountyData> entry : bountiesAtMarketById.entrySet()) {
            picker.add(entry.getKey(), entry.getValue().trigger_weight_mult);
        }

        for (int i = keysToReturn.size(); i < numberOfBountySlots; i++) {
            String pickedKey = picker.pickAndRemove();

            if (pickedKey != null) {
                keysToReturn.add(pickedKey);
            }
        }

        this.keysOfBountiesToShow = keysToReturn;
    }

    /**
     * Creates the optionData for when the player selects a bounty from the board.
     */
    private String getBountyDescriptionOptionKey(String key) {
        return "viewBountyDesc-" + key;
    }

    /**
     * Creates the optionData for when the player selects "Continue" after viewing an initial bounty description.
     */
    private String getBountyDetailsOptionKey(String key) {
        return "viewBountyDescDetails-" + key;
    }

    private Map<String, MagicBountyData.bountyData> getBountiesToShow() {
        Map<String, MagicBountyData.bountyData> ret = new HashMap<>(keysOfBountiesToShow.size());

        for (String key : keysOfBountiesToShow) {
            ret.put(key, MagicBountyData.BOUNTIES.get(key));
        }

        return ret;
    }

    /**
     * The max number of bounties to show at once.
     */
    private int getNumberOfBountySlots(MarketAPI market) {
        return MagicBountyCoordinator.getInstance().getBountySlotsAtMarket(market);
    }

    private void showFleet(
            TextPanelAPI info,
            float width,
            Color factionBaseUIColor,
            MagicBountyData.ShowFleet setting,
            List<FleetMemberAPI> ships,
            List<FleetMemberAPI> flagship,
            List<FleetMemberAPI> preset
    ) {

        int columns = 10;
        switch (setting) {
            case Text:
                //write the number of ships
                int num = ships.size();
                if (num < 5) {
                    num = 5;
                    info.addPara(getString("mb_fleet6"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor(),
                            "" + num
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet5"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
            case Flagship:
                //show the flagship
                info.addPara(getString("mb_fleet0") + getString("mb_fleet"));
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        flagship,
                        10f
                );
                info.addTooltip();
                break;
            case FlagshipText:
                //show the flagship
                info.addPara(getString("mb_fleet0") + getString("mb_fleet"));
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        flagship,
                        10f
                );
                info.addTooltip();

                //write the number of other ships
                num = ships.size() - 1;
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;

            case Preset:
                //show the preset fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));

                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) preset.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        preset,
                        10f
                );
                info.addTooltip();
                break;

            case PresetText:
                //show the preset fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));
                List<FleetMemberAPI> toShow = preset;
                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) preset.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();
                //write the number of other ships
                num = ships.size() - toShow.size();
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;

            case Vanilla:
                //show the Flagship and the 6 biggest ships in the fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));

                //there are less than 7 ships total, all will be shown
                if (ships.size() <= columns) {
                    toShow = new ArrayList<>();
                    //add flagship first
                    for (FleetMemberAPI m : ships) {
                        if (m.isFlagship()) {
                            toShow.add(m);
                            break;
                        }
                    }
                    //then all the rest
                    for (FleetMemberAPI m : ships) {
                        if (!m.isFlagship()) {
                            toShow.add(m);
                        }
                    }
                    //display the ships
                    info.beginTooltip().addShipList(
                            columns,
                            1,
                            (width - 10) / columns,
                            factionBaseUIColor,
                            toShow,
                            10f
                    );
                    info.addTooltip();
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                }
                //If there are more than 7 ships, pick the largest 7
                toShow = new ArrayList<>();
                //add flagship first
                for (FleetMemberAPI m : ships) {
                    if (m.isFlagship()) {
                        toShow.add(m);
                        break;
                    }
                }
                //then complete the list
                for (FleetMemberAPI m : ships) {
                    if (toShow.size() >= columns) break;
                    if (!m.isFlagship()) toShow.add(m);
                }
                //make the ship list
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();

                //write the number of other ships
                num = ships.size() - columns;
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;
            case All:
                //show the full fleet
                info.addPara(getString("mb_fleet2") + getString("mb_fleet"));
                toShow = new ArrayList<>();
                //add flagship first
                for (FleetMemberAPI m : ships) {
                    if (m.isFlagship()) {
                        toShow.add(m);
                        break;
                    }
                }
                //then all the rest
                for (FleetMemberAPI m : ships) {
                    if (!m.isFlagship()) {
                        toShow.add(m);
                    }
                }
                //display the ships
                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) ships.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();
            default:
                break;
        }
    }
}
