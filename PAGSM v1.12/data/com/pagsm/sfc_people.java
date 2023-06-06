/*package data.com.pagsm;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

public class sfc_people {
    public static boolean isDE = Global.getSettings().getModManager().isModEnabled("Diktat Enhancement");
    public static boolean isCSP = Global.getSettings().getModManager().isModEnabled("Csp");
    public static boolean isUAF = Global.getSettings().getModManager().isModEnabled("uaf");

    public static String sfckween = "sfckween"; //glorious science
    public static String sfcruni = "sfcruni"; //looks familiar
    public static String sfcruni2 = "sfcruni2"; //you have met a terrible fate haven't you
    public static String sfcyenni = "sfcyenni"; //also looks familiar
    public static String sfcmann = "sfcmann"; //2nd coolest old guy in the Sindrian Fuel Company
    public static String sfcruy = "sfcruy"; //envious of the lion's guard
    public static String sfcdunn = "sfcdunn"; //loves "surprise internship" camps
    public static String sfcfleures = "sfcfleures"; //brightest flower
    public static String sfcjenkins = "sfcjenkins"; //velmarie's sass
    public static String sfcvolturny = "sfcvolturny"; //best mascot
    public static String sfcfakeandrada = "sfcfakeandrada"; //the phillip andrada experience
    public static String sfchero = "sfchero"; //a true hero of the Sindrian Fuel Company
    public static String sfcrhea = "sfcrhea"; //a true hero's assistant of the Sindrian Fuel Company
    public static String sfcfonz = "sfcfonz"; //not the fonz
    public static String sfcarthur = "sfcarthur"; //religious zealot
    public static String dejalecto = "dejalecto"; //preacher for the Lion and DE integration
    public static String cspmongaera = "cspmongaera"; //deathless
    public static String nftpononzi = "nftpononzi"; //does he have an ape?
    public static String nftgimlet = "nftgimlet"; //ol' gimlet eye

    public static PersonAPI createAvaIfNeeded() {
        PersonAPI person = Global.getSector().getImportantPeople().getPerson(sfckween);
        if (person != null) return person;

        person = Global.getFactory().createPerson();
        person.setId(sfckween);
        person.setVoice(Voices.SOLDIER);
        person.setFaction(Factions.INDEPENDENT);
        person.setGender(FullName.Gender.FEMALE);
        person.setRankId(Ranks.SPACE_COMMANDER);
        person.setPostId(Ranks.POST_OFFICER);
        person.getName().setFirst(Sunrider_MiscFunctions.getString("avaNameFirst"));
        person.getName().setLast(Sunrider_MiscFunctions.getString("avaNameLast"));
        person.setPortraitSprite("graphics/portraits/Portrait_Ava.png");
        person.getMemoryWithoutUpdate().set("$chatterChar", "sunrider_ava");
        person.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);	// waifus do not die when killed

        // set skills (8 combat skills)
        person.getStats().setLevel(8);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        person.getStats().setSkillLevel("sunrider_SunridersMother", 2);
        //person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        //person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
        //person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);	// bonus

        Global.getSector().getImportantPeople().addPerson(person);
        return person;
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI volturn = Global.getSector().getEconomy().getMarket("volturn");
        if (volturn != null) {
            addGrandFuelFleet();
            MarketAPI nachiketa = Global.getSector().getEconomy().getMarket("nachiketa");
            if (nachiketa != null) {
                addNFTMerchantMilitia();
            }
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Askonia");
            MarketAPI market1 = Global.getSector().getEconomy().getMarket("sindria");


            if (market1 != null) {

                SectorEntityToken cnc = Global.getSector().getEntityById("diktat_cnc");
                cnc.setCustomDescriptionId("station_sindria_sindrian_fuel");

                // unrelenting genius
                PersonAPI sfckweenPerson = Global.getFactory().createPerson();
                sfckweenPerson.setId(sfckween);
                sfckweenPerson.setFaction(Factions.DIKTAT);
                sfckweenPerson.setGender(FullName.Gender.FEMALE);
                sfckweenPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfckweenPerson.setPostId("sfcheadresearcher");
                sfckweenPerson.setImportance(PersonImportance.HIGH);
                sfckweenPerson.setVoice(Voices.SCIENTIST);
                sfckweenPerson.getName().setFirst("Yunris");
                sfckweenPerson.getName().setLast("Kween");
                sfckweenPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfckween"));
                sfckweenPerson.addTag(Tags.CONTACT_UNDERWORLD);
                market1.getCommDirectory().addPerson(sfckweenPerson, 2);
                market1.addPerson(sfckweenPerson);
                ip.addPerson(sfckweenPerson);

                // lobster t-shirt
                PersonAPI sfcmannPerson = Global.getFactory().createPerson();
                sfcmannPerson.setId(sfcmann);
                sfcmannPerson.setFaction(Factions.DIKTAT);
                sfcmannPerson.setGender(FullName.Gender.MALE);
                sfcmannPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfcmannPerson.setPostId("sfcleaddeveloper");
                sfcmannPerson.setImportance(PersonImportance.HIGH);
                sfcmannPerson.setVoice(Voices.SCIENTIST);
                sfcmannPerson.getName().setFirst("Gregory");
                sfcmannPerson.getName().setLast("Mannfred");
                sfcmannPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcmann"));
                sfcmannPerson.addTag(Tags.CONTACT_TRADE);
                market1.getCommDirectory().addPerson(sfcmannPerson, 3);
                market1.addPerson(sfcmannPerson);
                ip.addPerson(sfcmannPerson);

                // a regular Yunifer
                PersonAPI sfcruniPerson = Global.getFactory().createPerson();
                sfcruniPerson.setId(sfcruni);
                sfcruniPerson.setFaction(Factions.DIKTAT);
                sfcruniPerson.setGender(FullName.Gender.FEMALE);
                sfcruniPerson.setRankId(Ranks.SPACE_CAPTAIN);
                sfcruniPerson.setPostId(Ranks.POST_OFFICER);
                sfcruniPerson.setPersonality(Personalities.TIMID);
                sfcruniPerson.setImportance(PersonImportance.VERY_LOW);
                sfcruniPerson.setVoice(Voices.SOLDIER);
                sfcruniPerson.getName().setFirst("Yunifer");
                sfcruniPerson.getName().setLast("Runi");
                sfcruniPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcruni"));
                sfcruniPerson.getStats().setSkillLevel("sfc_iapetus", 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
                sfcruniPerson.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
                sfcruniPerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
                sfcruniPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
                sfcruniPerson.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
                sfcruniPerson.getStats().setLevel(10);
                sfcruniPerson.getMemoryWithoutUpdate().set("$chatterChar", "sfcruni");
                sfcruniPerson.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);
                sfcruniPerson.addTag("coff_nocapture");

                market1.getCommDirectory().addPerson(sfcruniPerson);
                market1.addPerson(sfcruniPerson);
                market1.getCommDirectory().getEntryForPerson(sfcruniPerson).setHidden(true);
                ip.addPerson(sfcruniPerson);


                //a mistake was made
                PersonAPI sfcruni2Person = Global.getFactory().createPerson();
                sfcruni2Person.setId(sfcruni2);
                sfcruni2Person.setFaction(Factions.DIKTAT);
                sfcruni2Person.setGender(FullName.Gender.FEMALE);
                sfcruni2Person.setRankId(Ranks.SPACE_CAPTAIN);
                sfcruni2Person.setPostId(Ranks.POST_OFFICER);
                sfcruni2Person.setPersonality(Personalities.STEADY);
                sfcruni2Person.setImportance(PersonImportance.VERY_LOW);
                sfcruni2Person.setVoice(Voices.SOLDIER);
                sfcruni2Person.getName().setFirst("Yunifer");
                sfcruni2Person.getName().setLast("Runi");
                sfcruni2Person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcruni2"));
                sfcruni2Person.getStats().setLevel(1);
                sfcruni2Person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
                sfcruni2Person.getMemoryWithoutUpdate().set("$chatterChar", "sfcthisofficer");
                sfcruni2Person.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);

                market1.getCommDirectory().addPerson(sfcruni2Person);
                market1.addPerson(sfcruni2Person);
                market1.getCommDirectory().getEntryForPerson(sfcruni2Person).setHidden(true);
                ip.addPerson(sfcruni2Person);

                // magic eight ball
                PersonAPI sfcfakeandradaPerson = Global.getFactory().createPerson();
                sfcfakeandradaPerson.setId(sfcfakeandrada);
                sfcfakeandradaPerson.setFaction(Factions.NEUTRAL);
                sfcfakeandradaPerson.setGender(FullName.Gender.MALE);
                sfcfakeandradaPerson.setRankId("Hologram");
                sfcfakeandradaPerson.setPostId("Hologram");
                sfcfakeandradaPerson.getName().setFirst("Phillip");
                sfcfakeandradaPerson.getName().setLast("Andrada");
                sfcfakeandradaPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcfakeandrada"));
                ip.addPerson(sfcfakeandradaPerson);

                // what kind of name is fuel frontal?
                PersonAPI sfcheroPerson = Global.getFactory().createPerson();
                sfcheroPerson.setId(sfchero);
                sfcheroPerson.setFaction(Factions.DIKTAT);
                sfcheroPerson.setGender(FullName.Gender.MALE);
                sfcheroPerson.setRankId(Ranks.SPACE_CAPTAIN);
                sfcheroPerson.setPostId(Ranks.POST_OFFICER);
                sfcheroPerson.setImportance(PersonImportance.VERY_HIGH);
                sfcheroPerson.setVoice(Voices.SOLDIER);
                sfcheroPerson.getName().setFirst("Fuel");
                sfcheroPerson.getName().setLast("Frontal");
                sfcheroPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfchero"));
                ip.addPerson(sfcheroPerson);

                // short
                PersonAPI sfcrheaPerson = Global.getFactory().createPerson();
                sfcrheaPerson.setId(sfcrhea);
                sfcrheaPerson.setFaction(Factions.DIKTAT);
                sfcrheaPerson.setGender(FullName.Gender.FEMALE);
                sfcrheaPerson.setRankId(Ranks.SPACE_LIEUTENANT);
                sfcrheaPerson.setPostId(Ranks.POST_AGENT);
                sfcrheaPerson.setImportance(PersonImportance.MEDIUM);
                sfcrheaPerson.setVoice(Voices.SOLDIER);
                sfcrheaPerson.getName().setFirst("Sindy");
                sfcrheaPerson.getName().setLast("Rhea");
                sfcrheaPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcrhea"));
                ip.addPerson(sfcrheaPerson);
            }


            market1 = Global.getSector().getEconomy().getMarket("cruor");
            if (market1 != null) {

                market1.addSubmarket("sfc_bsamarket");

                SectorEntityToken cruor = system.getEntityById("cruor");
                cruor.getMarket().addIndustry(Industries.PATROLHQ);
                cruor.getMarket().addIndustry("sfclionsoutpost");

                // gets it dunn
                PersonAPI sfcdunnPerson = Global.getFactory().createPerson();
                sfcdunnPerson.setId(sfcdunn);
                sfcdunnPerson.setFaction(Factions.DIKTAT);
                sfcdunnPerson.setGender(FullName.Gender.MALE);
                sfcdunnPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfcdunnPerson.setPostId("sfclaborchief");
                sfcdunnPerson.setImportance(PersonImportance.HIGH);
                sfcdunnPerson.getName().setFirst("Meridin");
                sfcdunnPerson.getName().setLast("Dunn");
                sfcdunnPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcdunn"));
                sfcdunnPerson.addTag(Tags.CONTACT_TRADE);
                sfcdunnPerson.setVoice(Voices.SPACER);
                sfcdunnPerson.getStats().setSkillLevel("sfc_hardwork", 1);
                sfcdunnPerson.getStats().setSkillLevel("sfc_gilded", 1);
                market1.setAdmin(sfcdunnPerson);
                market1.getCommDirectory().addPerson(sfcdunnPerson, 0);
                market1.addPerson(sfcdunnPerson);
                ip.addPerson(sfcdunnPerson);

                // hates the Lion's Guard, and his job
                PersonAPI sfcruyPerson = Global.getFactory().createPerson();
                sfcruyPerson.setId(sfcruy);
                sfcruyPerson.setFaction(Factions.DIKTAT);
                sfcruyPerson.setGender(FullName.Gender.MALE);
                sfcruyPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfcruyPerson.setPostId("sfcsecchief");
                sfcruyPerson.setImportance(PersonImportance.MEDIUM);
                sfcruyPerson.getName().setFirst("Rudric");
                sfcruyPerson.getName().setLast("Ruy");
                sfcruyPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcruy"));
                sfcruyPerson.addTag(Tags.CONTACT_MILITARY);
                sfcruyPerson.setVoice(Voices.SOLDIER);
                market1.getCommDirectory().addPerson(sfcruyPerson, 1);
                market1.addPerson(sfcruyPerson);
                ip.addPerson(sfcruyPerson);

                // CSP reference
                PersonAPI cspmongaeraPerson = Global.getFactory().createPerson();
                cspmongaeraPerson.setId(cspmongaera);
                cspmongaeraPerson.setFaction(Factions.LIONS_GUARD);
                cspmongaeraPerson.setGender(FullName.Gender.FEMALE);
                cspmongaeraPerson.setRankId("sfcregmanager");
                cspmongaeraPerson.setPostId("sfcregmanager");
                cspmongaeraPerson.setImportance(PersonImportance.VERY_HIGH);
                cspmongaeraPerson.getName().setFirst("Cayista");
                cspmongaeraPerson.getName().setLast("Mongaera");
                cspmongaeraPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "cspmongaera"));
                cspmongaeraPerson.addTag(Tags.CONTACT_MILITARY);
                cspmongaeraPerson.setVoice(Voices.SOLDIER);
                market1.getCommDirectory().addPerson(cspmongaeraPerson, 0);
                market1.addPerson(cspmongaeraPerson);
                market1.getCommDirectory().getEntryForPerson(cspmongaeraPerson).setHidden(true);
                ip.addPerson(cspmongaeraPerson);

            }

            market1 = Global.getSector().getEconomy().getMarket("volturn");
            if (market1 != null) {

                SectorEntityToken volturncolony = system.getEntityById("volturn");
                volturncolony.getMarket().getIndustry(Industries.AQUACULTURE).setSpecialItem(new SpecialItemData("sfc_aquaticstimulator", null));
                volturncolony.getMarket().addIndustry("sfclobsterresort");

                // sunny flower
                PersonAPI sfcfleuresPerson = Global.getFactory().createPerson();
                sfcfleuresPerson.setId(sfcfleures);
                sfcfleuresPerson.setFaction(Factions.DIKTAT);
                sfcfleuresPerson.setGender(FullName.Gender.FEMALE);
                sfcfleuresPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfcfleuresPerson.setPostId("sfcadvertchief");
                sfcfleuresPerson.setImportance(PersonImportance.HIGH);
                sfcfleuresPerson.getName().setFirst("Lumi");
                sfcfleuresPerson.getName().setLast("Fleures");
                sfcfleuresPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcfleures"));
                sfcfleuresPerson.addTag(Tags.CONTACT_TRADE);
                sfcfleuresPerson.setVoice(Voices.OFFICIAL);
                sfcfleuresPerson.getStats().setSkillLevel("sfc_hardwork", 1);
                market1.setAdmin(sfcfleuresPerson);
                market1.getCommDirectory().addPerson(sfcfleuresPerson, 0);
                market1.addPerson(sfcfleuresPerson);
                ip.addPerson(sfcfleuresPerson);

                // needs coffee
                PersonAPI sfcjenkinsPerson = Global.getFactory().createPerson();
                sfcjenkinsPerson.setId(sfcjenkins);
                sfcjenkinsPerson.setFaction(Factions.DIKTAT);
                sfcjenkinsPerson.setGender(FullName.Gender.FEMALE);
                sfcjenkinsPerson.setRankId(Ranks.SPACE_ADMIRAL);
                sfcjenkinsPerson.setPostId("sfcproductionchief");
                sfcjenkinsPerson.setImportance(PersonImportance.HIGH);
                sfcjenkinsPerson.getName().setFirst("Velmarie");
                sfcjenkinsPerson.getName().setLast("Jenkins");
                sfcjenkinsPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcjenkins"));
                sfcjenkinsPerson.addTag(Tags.CONTACT_TRADE);
                sfcjenkinsPerson.setVoice(Voices.OFFICIAL);
                market1.getCommDirectory().addPerson(sfcjenkinsPerson, 1);
                market1.addPerson(sfcjenkinsPerson);
                ip.addPerson(sfcjenkinsPerson);

                // lober
                PersonAPI sfcvolturnyLobster = Global.getFactory().createPerson();
                sfcvolturnyLobster.setId(sfcvolturny);
                sfcvolturnyLobster.setFaction(Factions.DIKTAT);
                sfcvolturnyLobster.setGender(FullName.Gender.MALE);
                sfcvolturnyLobster.setRankId(Ranks.CITIZEN);
                sfcvolturnyLobster.setPostId(Ranks.POST_UNKNOWN);
                sfcvolturnyLobster.setImportance(PersonImportance.VERY_HIGH);
                sfcvolturnyLobster.getName().setFirst("Volturny");
                sfcvolturnyLobster.getName().setLast("");
                sfcvolturnyLobster.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcvolturny"));


                market1.getCommDirectory().addPerson(sfcvolturnyLobster);
                market1.getCommDirectory().getEntryForPerson(sfcvolturnyLobster).setHidden(true);
                market1.addPerson(sfcvolturnyLobster);
                ip.addPerson(sfcvolturnyLobster);

                // hey fonzi
                PersonAPI sfcfonzPerson = Global.getFactory().createPerson();
                sfcfonzPerson.setId(sfcfonz);
                sfcfonzPerson.setFaction(Factions.LIONS_GUARD);
                sfcfonzPerson.setGender(FullName.Gender.MALE);
                sfcfonzPerson.setRankId("sfcregmanager");
                sfcfonzPerson.setPostId("sfcregmanager");
                sfcfonzPerson.setImportance(PersonImportance.VERY_HIGH);
                sfcfonzPerson.getName().setFirst("Albert");
                sfcfonzPerson.getName().setLast("Fonziphone");
                sfcfonzPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcfonz"));
                sfcfonzPerson.addTag(Tags.CONTACT_MILITARY);
                sfcfonzPerson.setVoice(Voices.SOLDIER);
                market1.getCommDirectory().addPerson(sfcfonzPerson, 0);
                market1.addPerson(sfcfonzPerson);
                market1.getCommDirectory().getEntryForPerson(sfcfonzPerson).setHidden(true);
                ip.addPerson(sfcfonzPerson);
            }
            //the nft starts here
            market1 = Global.getSector().getEconomy().getMarket("nachiketa");
            if (market1 != null) {
                PersonAPI nftpononziPerson = Global.getFactory().createPerson();
                nftpononziPerson.setId(nftpononzi);
                nftpononziPerson.setFaction(Factions.HEGEMONY);
                nftpononziPerson.setGender(FullName.Gender.MALE);
                nftpononziPerson.setRankId("nftexecutive");
                nftpononziPerson.setPostId("nftexecutive");
                nftpononziPerson.setImportance(PersonImportance.VERY_HIGH);
                nftpononziPerson.getName().setFirst("Karlos");
                nftpononziPerson.getName().setLast("Pononzi");
                nftpononziPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nftpononzi"));
                nftpononziPerson.addTag(Tags.CONTACT_TRADE);
                nftpononziPerson.setVoice(Voices.BUSINESS);
                market1.getCommDirectory().addPerson(nftpononziPerson, 1);
                market1.addPerson(nftpononziPerson);
                ip.addPerson(nftpononziPerson);
            }

            market1 = Global.getSector().getEconomy().getMarket("nortia");
            if (market1 != null) {
                PersonAPI nftgimletPerson = Global.getFactory().createPerson();
                nftgimletPerson.setId(nftgimlet);
                nftgimletPerson.setFaction(Factions.HEGEMONY);
                nftgimletPerson.setGender(FullName.Gender.MALE);
                nftgimletPerson.setRankId(Ranks.GROUND_GENERAL);
                nftgimletPerson.setPostId(Ranks.POST_SUPPLY_OFFICER);
                nftgimletPerson.setImportance(PersonImportance.HIGH);
                nftgimletPerson.getName().setFirst("Darington");
                nftgimletPerson.getName().setLast("Gimlet");
                nftgimletPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nftgimlet"));
                nftgimletPerson.addTag(Tags.CONTACT_MILITARY);
                nftgimletPerson.setVoice(Voices.SOLDIER);
                market1.getCommDirectory().addPerson(nftgimletPerson, 1);
                market1.addPerson(nftgimletPerson);
                ip.addPerson(nftgimletPerson);
            }

            market1 = Global.getSector().getEconomy().getMarket("umbra");
            if (market1 != null) {

                SectorEntityToken umbra = system.getEntityById("umbra");
                umbra.getMarket().getIndustry(Industries.MINING).setSpecialItem(new SpecialItemData("sfc_motemegacondenser", null));
            }

            market1 =  Global.getSector().getEconomy().getMarket("epiphany");
            if (market1 != null) {
                PersonAPI person = Global.getFactory().createPerson();
                person.setId(sfcarthur);
                person.setFaction(Factions.LUDDIC_PATH);
                person.setGender(FullName.Gender.MALE);
                person.setRankId(Ranks.BROTHER);
                person.setPostId(Ranks.POST_TERRORIST);
                person.setImportance(PersonImportance.HIGH);
                person.getName().setFirst("Bellier");
                person.getName().setLast("Arthur");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcarthur"));

                market1.getCommDirectory().addPerson(person);
                market1.getCommDirectory().getEntryForPerson(person).setHidden(true);
                market1.addPerson(person);
                ip.addPerson(person);
            }


            PersonAPI sec_officer = Global.getSector().getImportantPeople().getPerson("sec_officer");
            if (sec_officer != null) {
                sec_officer.setRankId("sfcjunior");
                sec_officer.setPostId("sfcjunior");
                sec_officer.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcbalashi"));
                sec_officer.addTag(Tags.CONTACT_MILITARY);
            }

            PersonAPI andrada = Global.getSector().getImportantPeople().getPerson("andrada");
            if (andrada != null) {
                andrada.setImportance(PersonImportance.VERY_HIGH);
                andrada.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcandrada"));
                andrada.getStats().setSkillLevel("sfc_hardwork", 1);
            }
            // DE reference

            if (!isDE) {
                MarketAPI sindriade = null;
                sindriade = Global.getSector().getEconomy().getMarket("sindria");
                if (sindriade != null) {
                    PersonAPI dejalectoPerson = Global.getFactory().createPerson();
                    dejalectoPerson.setId(dejalecto);
                    dejalectoPerson.setFaction(Factions.LIONS_GUARD);
                    dejalectoPerson.setGender(FullName.Gender.MALE);
                    dejalectoPerson.setRankId("sfcexecutive");
                    dejalectoPerson.setPostId("sfcexecutive");
                    dejalectoPerson.setImportance(PersonImportance.VERY_HIGH);
                    dejalectoPerson.getName().setFirst("Randall");
                    dejalectoPerson.getName().setLast("Jalecto");
                    dejalectoPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dejalecto"));
                    sindriade.getCommDirectory().addPerson(dejalectoPerson, 1);
                    sindriade.addPerson(dejalectoPerson);
                    sindriade.getCommDirectory().getEntryForPerson(dejalectoPerson).setHidden(true);
                    ip.addPerson(dejalectoPerson);
                }
            }
            if (isDE) {
                boolean DEenablelitemode = Global.getSettings().getBoolean("DEenablelitemode");
                boolean DEenablefortressmode = Global.getSettings().getBoolean("DEenablefortressmode");
                MarketAPI market1de = null;
                MarketAPI market2de = null;
                if (!DEenablelitemode) {
                    if (!DEenablefortressmode) {
                        //market1de = Global.getSector().getEconomy().getMarket("ryzan_supercomplex");
                        market1de = Global.getSector().getStarSystem("Andor").getEntityById("ryzan_supercomplex").getMarket();
                        if (market1de != null) {
                            PersonAPI dejalectoPerson = Global.getFactory().createPerson();
                            dejalectoPerson.setId(dejalecto);
                            dejalectoPerson.setFaction(Factions.LIONS_GUARD);
                            dejalectoPerson.setGender(FullName.Gender.MALE);
                            dejalectoPerson.setRankId("sfcexecutive");
                            dejalectoPerson.setPostId("sfcregmanager");
                            dejalectoPerson.setImportance(PersonImportance.VERY_HIGH);
                            dejalectoPerson.getName().setFirst("Randall");
                            dejalectoPerson.getName().setLast("Jalecto");
                            dejalectoPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dejalecto"));
                            dejalectoPerson.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
                            dejalectoPerson.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
                            market1de.setAdmin(dejalectoPerson);
                            market1de.getCommDirectory().addPerson(dejalectoPerson, 0);
                            market1de.addPerson(dejalectoPerson);
                            ip.addPerson(dejalectoPerson);
                        }
                    } else {
                        market2de = Global.getSector().getEconomy().getMarket("sindria");
                        if (market2de != null) {
                            PersonAPI dejalectoPerson = Global.getFactory().createPerson();
                            dejalectoPerson.setId(dejalecto);
                            dejalectoPerson.setFaction(Factions.LIONS_GUARD);
                            dejalectoPerson.setGender(FullName.Gender.MALE);
                            dejalectoPerson.setRankId("sfcexecutive");
                            dejalectoPerson.setPostId("sfcexecutive");
                            dejalectoPerson.setImportance(PersonImportance.VERY_HIGH);
                            dejalectoPerson.getName().setFirst("Randall");
                            dejalectoPerson.getName().setLast("Jalecto");
                            dejalectoPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dejalecto"));
                            market2de.getCommDirectory().addPerson(dejalectoPerson, 1);
                            market2de.addPerson(dejalectoPerson);
                            market2de.getCommDirectory().getEntryForPerson(dejalectoPerson).setHidden(true);
                            ip.addPerson(dejalectoPerson);
                        }
                    }
                } else {
                    market2de = Global.getSector().getEconomy().getMarket("sindria");
                    if (market2de != null) {
                        PersonAPI dejalectoPerson = Global.getFactory().createPerson();
                        dejalectoPerson.setId(dejalecto);
                        dejalectoPerson.setFaction(Factions.LIONS_GUARD);
                        dejalectoPerson.setGender(FullName.Gender.MALE);
                        dejalectoPerson.setRankId("sfcexecutive");
                        dejalectoPerson.setPostId("sfcexecutive");
                        dejalectoPerson.setImportance(PersonImportance.VERY_HIGH);
                        dejalectoPerson.getName().setFirst("Randall");
                        dejalectoPerson.getName().setLast("Jalecto");
                        dejalectoPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dejalecto"));
                        market2de.getCommDirectory().addPerson(dejalectoPerson, 1);
                        market2de.addPerson(dejalectoPerson);
                        market2de.getCommDirectory().getEntryForPerson(dejalectoPerson).setHidden(true);
                        ip.addPerson(dejalectoPerson);
                    }
                }
            }
            // CSP reference
        }
    }
}*/
