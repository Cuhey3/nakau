package com.heroku.nakau;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpandNakau {

    public static void main(String[] args) throws IOException {
        InputStream resourceAsStream
                = ExpandNakau.class.getClassLoader().getResourceAsStream("menu.json");
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(resourceAsStream, "UTF-8"))) {
            String json = buffer.lines().collect(Collectors.joining("\n"));
            List<Map<String, Object>> menuList = new Gson().fromJson(json, List.class);
            List<Map<String, Object>> result = menuList.stream().filter((map) -> {
                String menuString = (String) map.get("menu");
                return menuString.equals("teishoku") || menuString.equals("morning") || menuString.equals("child");
            }).flatMap((map) -> expandMapToList(map)).collect(Collectors.toList());
            List<Map<String, Object>> mainMenu = menuList.stream().filter((map) -> {
                String menuString = (String) map.get("menu");
                return menuString.equals("main");
            }).flatMap((map) -> expandMapToList(map)).collect(Collectors.toList());
            result.addAll(mainMenu);
            List<Map<String, Object>> setMenu = menuList.stream().filter((map) -> {
                String menuString = (String) map.get("menu");
                return menuString.equals("set");
            }).flatMap((map) -> expandMapToList(map)).collect(Collectors.toList());
            mainMenu.stream().flatMap((main) -> mixMainAndSet(main, setMenu))
                    .forEach(result::add);
            result.sort((Map o1, Map o2) -> (int) ((Double) o1.get("kcal") - (Double) o2.get("kcal")));
            result = result.stream().map((map) -> {
                map.put("price", ((Double) map.get("price")).intValue());
                map.put("kcal", ((Double) map.get("kcal")).intValue());
                return map;
            }).collect(Collectors.toList());
            System.out.println(result.size());
            String resultJson = new GsonBuilder().setPrettyPrinting().create().toJson(result);
            System.out.println(resultJson);
        }
    }

    public static Stream<Map<String, Object>> expandMapToList(Map<String, Object> compressed) {
        List<Map<String, Object>> valList = (List<Map<String, Object>>) compressed.get("val");
        return valList.stream().map((val) -> {
            Map<String, Object> expanded = new LinkedHashMap<>();
            expanded.putAll(compressed);
            expanded.remove("val");
            expanded.putAll(val);
            return expanded;
        });
    }

    public static Stream<Map<String, Object>> mixMainAndSet(Map<String, Object> mainMenu, List<Map<String, Object>> setMenuList) {
        return setMenuList.stream().map((set) -> mix(mainMenu, set));
    }

    public static Map<String, Object> mix(Map<String, Object> mix1, Map<String, Object> mix2) {
        LinkedHashMap<String, Object> mixed = new LinkedHashMap<>();
        List urls = new ArrayList<>();
        urls.add(mix1.get("url"));
        urls.add(mix2.get("url"));
        mixed.put("urls", urls);
        List titles = new ArrayList<>();
        titles.add(mix1.get("title"));
        titles.add(mix2.get("title"));
        mixed.put("titles", titles);
        mixed.put("menu", "mixed");
        mixed.put("takeout", false);
        List types = new ArrayList<>();
        types.add(mix1.get("type"));
        types.add(mix2.get("type"));
        mixed.put("types", types);
        Double price = (Double) mix1.get("price") + (Double) mix2.get("price");
        mixed.put("price", price);
        List quickNos = new ArrayList<>();
        quickNos.add(mix1.get("quickNo"));
        quickNos.add(mix2.get("quickNo"));
        mixed.put("quickNos", quickNos);
        Double kcal = (Double) mix1.get("kcal") + (Double) mix2.get("kcal");
        mixed.put("kcal", kcal);
        List imgs = new ArrayList<>();
        imgs.add(mix1.get("img"));
        imgs.add(mix2.get("img"));
        mixed.put("imgs", imgs);
        return mixed;
    }
}
