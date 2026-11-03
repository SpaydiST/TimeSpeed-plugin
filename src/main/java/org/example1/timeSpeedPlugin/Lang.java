package org.example1.timeSpeedPlugin;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Локализация: тексты лежат в lang/*.yml (в jar и в папке плагина),
 * в Java остаётся только карта локалей Minecraft -> код языка.
 */
public final class Lang {

    private static final Map<String, Map<String, String>> LANGUAGES = new LinkedHashMap<>();
    private static final Map<String, String> LOCALE_TO_LANG = new HashMap<>();
    private static final String DEFAULT_LANG = "en";
    private static final String LANG_FOLDER = "lang";

    static {
        // Карта всех локлей Minecraft -> ближайший поддерживаемый язык
        putLoc("af_za", "en");
        putLoc("ar_sa", "ar");
        putLoc("ast_es", "es");
        putLoc("az_az", "tr");
        putLoc("ba_ru", "ru");
        putLoc("bar", "de");
        putLoc("be_by", "ru");
        putLoc("bg_bg", "bg");
        putLoc("br_fr", "fr");
        putLoc("brb", "nl");
        putLoc("bs_ba", "hr");
        putLoc("ca_es", "es");
        putLoc("cs_cz", "cs");
        putLoc("cy_gb", "en");
        putLoc("da_dk", "da");
        putLoc("de_at", "de");
        putLoc("de_ch", "de");
        putLoc("de_de", "de");
        putLoc("el_gr", "el");
        putLoc("en_au", "en");
        putLoc("en_ca", "en");
        putLoc("en_gb", "en");
        putLoc("en_nz", "en");
        putLoc("en_pt", "en");
        putLoc("en_ud", "en");
        putLoc("en_us", "en");
        putLoc("enp", "en");
        putLoc("enws", "en");
        putLoc("eo_uy", "en");
        putLoc("es_419", "es");
        putLoc("es_ar", "es");
        putLoc("es_cl", "es");
        putLoc("es_es", "es");
        putLoc("es_mx", "es");
        putLoc("es_uy", "es");
        putLoc("es_ve", "es");
        putLoc("et_ee", "fi");
        putLoc("eu_es", "es");
        putLoc("fa_ir", "fa");
        putLoc("fi_fi", "fi");
        putLoc("fil_ph", "en");
        putLoc("fo_fo", "da");
        putLoc("fr_ca", "fr");
        putLoc("fr_fr", "fr");
        putLoc("frp", "fr");
        putLoc("fy_nl", "nl");
        putLoc("ga_ie", "en");
        putLoc("gd_gb", "en");
        putLoc("gl_es", "es");
        putLoc("haw_us", "en");
        putLoc("he_il", "he");
        putLoc("hi_in", "hi");
        putLoc("hr_hr", "hr");
        putLoc("hu_hu", "hu");
        putLoc("hy_am", "ru");
        putLoc("id_id", "id");
        putLoc("ig_ng", "en");
        putLoc("io_en", "en");
        putLoc("is_is", "no");
        putLoc("isv", "ru");
        putLoc("it_it", "it");
        putLoc("ja_jp", "ja");
        putLoc("jbo_en", "en");
        putLoc("ka_ge", "ru");
        putLoc("kk_kz", "ru");
        putLoc("kn_in", "hi");
        putLoc("ko_kr", "ko");
        putLoc("ksh", "de");
        putLoc("kw_gb", "en");
        putLoc("la_la", "en");
        putLoc("lb_lu", "de");
        putLoc("li_li", "nl");
        putLoc("lt_lt", "ru");
        putLoc("lv_lv", "ru");
        putLoc("lzh", "zh_cn");
        putLoc("mk_mk", "bg");
        putLoc("mn_mn", "ru");
        putLoc("ms_my", "ms");
        putLoc("mt_mt", "it");
        putLoc("nah", "es");
        putLoc("nds_de", "de");
        putLoc("nl_be", "nl");
        putLoc("nl_nl", "nl");
        putLoc("nn_no", "no");
        putLoc("no_no", "no");
        putLoc("nuk", "en");
        putLoc("oc_fr", "fr");
        putLoc("oj_ca", "en");
        putLoc("ovd", "sv");
        putLoc("pl_pl", "pl");
        putLoc("pt_br", "pt");
        putLoc("pt_pt", "pt");
        putLoc("qya_aa", "en");
        putLoc("ro_ro", "ro");
        putLoc("rpr", "uk");
        putLoc("ru_ru", "ru");
        putLoc("ry_ua", "uk");
        putLoc("sah_sah", "ru");
        putLoc("se_no", "no");
        putLoc("sk_sk", "sk");
        putLoc("sl_si", "hr");
        putLoc("so_so", "en");
        putLoc("sq_al", "it");
        putLoc("sr_cs", "hr");
        putLoc("sr_sp", "hr");
        putLoc("sv_se", "sv");
        putLoc("sxu", "de");
        putLoc("szl", "pl");
        putLoc("ta_in", "hi");
        putLoc("th_th", "th");
        putLoc("tl_ph", "en");
        putLoc("tlh_aa", "en");
        putLoc("tok", "en");
        putLoc("tr_tr", "tr");
        putLoc("tt_ru", "ru");
        putLoc("tzl_tzl", "it");
        putLoc("uk_ua", "uk");
        putLoc("val_es", "es");
        putLoc("vec_it", "it");
        putLoc("vi_vn", "vi");
        putLoc("yi_de", "de");
        putLoc("yo_ng", "en");
        putLoc("zh_cn", "zh_cn");
        putLoc("zh_hk", "zh_tw");
        putLoc("zh_tw", "zh_tw");
        putLoc("zlm_arab", "ms");
    }

    private Lang() {}

    /**
     * Выгружает lang/*.yml из jar в папку плагина (если их там ещё нет)
     * и загружает все языки из папки плагина.
     */
    public static void load(JavaPlugin plugin) {
        LANGUAGES.clear();
        saveDefaultLangs(plugin);
        loadLangFiles(plugin);
        if (!LANGUAGES.containsKey(DEFAULT_LANG)) {
            plugin.getLogger().severe("Missing default language file: " + LANG_FOLDER + "/" + DEFAULT_LANG + ".yml");
        }
    }

    private static void saveDefaultLangs(JavaPlugin plugin) {
        File dir = new File(plugin.getDataFolder(), LANG_FOLDER);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            plugin.getLogger().severe("Could not create folder " + dir.getPath());
            return;
        }
        File jarFile;
        try {
            jarFile = new File(Lang.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            plugin.getLogger().warning("Could not locate plugin jar to extract languages: " + e.getMessage());
            return;
        }
        if (!jarFile.isFile()) return; // запуск из IDE/классов — не jar
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> seen = new HashSet<>();
            while (entries.hasMoreElements()) {
                String path = entries.nextElement().getName();
                if (!path.startsWith(LANG_FOLDER + "/") || path.endsWith("/")) continue;
                String fileName = path.substring(LANG_FOLDER.length() + 1);
                if (fileName.isEmpty() || !fileName.endsWith(".yml") || !seen.add(fileName)) continue;
                if (!new File(dir, fileName).exists()) {
                    plugin.saveResource(LANG_FOLDER + "/" + fileName, false);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to extract default languages: " + e.getMessage());
        }
    }

    private static void loadLangFiles(JavaPlugin plugin) {
        File[] files = new File(plugin.getDataFolder(), LANG_FOLDER).listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            String lang = file.getName()
                    .substring(0, file.getName().length() - ".yml".length())
                    .toLowerCase(Locale.ROOT);
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getLogger().warning("Could not load " + LANG_FOLDER + "/" + file.getName() + ": " + e.getMessage());
                continue;
            }
            try (InputStream defStream = plugin.getResource(LANG_FOLDER + "/" + file.getName())) {
                if (defStream != null) {
                    YamlConfiguration defYaml = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defStream, StandardCharsets.UTF_8));
                    boolean updated = false;
                    for (String key : defYaml.getKeys(false)) {
                        if (!yaml.contains(key)) {
                            yaml.set(key, defYaml.get(key));
                            updated = true;
                        }
                    }
                    if (updated) {
                        yaml.save(file);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not update " + LANG_FOLDER + "/" + file.getName() + ": " + e.getMessage());
            }
            Map<String, String> table = new HashMap<>();
            for (String key : yaml.getKeys(false)) {
                String value = yaml.getString(key);
                if (value != null) table.put(key, value);
            }
            if (table.isEmpty()) {
                plugin.getLogger().warning("Language file " + file.getName() + " is empty, skipped.");
                continue;
            }
            LANGUAGES.put(lang, table);
        }
    }

    public static String get(Player player, String key) {
        return get(player != null ? player.locale() : null, key);
    }

    public static String get(Locale locale, String key) {
        String lang = langFor(locale);
        Map<String, String> defaultTable = LANGUAGES.getOrDefault(DEFAULT_LANG, Map.of());
        Map<String, String> table = LANGUAGES.getOrDefault(lang, defaultTable);
        return table.getOrDefault(key, defaultTable.getOrDefault(key, key));
    }

    public static String getConsole(String key) {
        return get(Locale.ENGLISH, key);
    }

    public static String get(Player player, String key, Object... args) {
        return format(get(player, key), args);
    }

    public static String format(String message, Object... args) {
        if (args.length > 0 && message.contains("%s")) {
            return String.format(Locale.ROOT, message, args);
        }
        return message;
    }

    private static String langFor(Locale locale) {
        if (locale == null) return DEFAULT_LANG;
        String lang = locale.getLanguage().toLowerCase();
        String country = locale.getCountry();
        String tag = country.isEmpty() ? lang : lang + "_" + country.toLowerCase();
        String mapped = LOCALE_TO_LANG.get(tag);
        if (mapped != null) return mapped;
        String byLang = LOCALE_TO_LANG.get(lang);
        if (byLang != null) return byLang;
        if (LANGUAGES.containsKey(lang)) return lang;
        return DEFAULT_LANG;
    }

    private static void putLoc(String code, String lang) {
        LOCALE_TO_LANG.put(code, lang);
    }
}
