import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.Locale;

public class YmlToJava {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("shame.yml")));
        
        Matcher startPosMatcher = Pattern.compile("start_position:\\s*\\n-\\s*([\\d\\.-]+)\\s*\\n-\\s*([\\d\\.-]+)\\s*\\n-\\s*([\\d\\.-]+)").matcher(content);
        double[] startPos = {0, 0, 0};
        if (startPosMatcher.find()) {
            startPos[0] = Double.parseDouble(startPosMatcher.group(1));
            startPos[1] = Double.parseDouble(startPosMatcher.group(2));
            startPos[2] = Double.parseDouble(startPosMatcher.group(3));
        }

        Map<Integer, List<String>> particles = parseSection(content, "particles");
        Map<Integer, List<String>> sounds = parseSection(content, "sounds");

        List<Integer> allTicks = new ArrayList<>(particles.keySet());
        for (Integer t : sounds.keySet()) if (!allTicks.contains(t)) allTicks.add(t);
        Collections.sort(allTicks);

        List<Integer> startTicks = new ArrayList<>();
        List<Integer> finishTicks = new ArrayList<>();
        for (int t : allTicks) {
            if (t < 0) startTicks.add(t);
            else if (t > 1000) finishTicks.add(t);
        }

        int startBase = startTicks.isEmpty() ? 0 : startTicks.get(0);
        int finishBase = finishTicks.isEmpty() ? 0 : finishTicks.get(0);

        // Find anchors
        double[] startAnchor = startPos;
        if (!startTicks.isEmpty()) {
            List<String> ps = particles.get(startTicks.get(0));
            if (ps != null && !ps.isEmpty()) {
                String firstP = ps.get(0);
                Matcher m = Pattern.compile("new P\\(([\\d\\.-]+)f, ([\\d\\.-]+)f, ([\\d\\.-]+)f").matcher(firstP);
                if (m.find()) {
                    startAnchor = new double[]{Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))};
                }
            }
        }

        double[] finishAnchor = startAnchor;
        if (!finishTicks.isEmpty()) {
            List<String> ps = particles.get(finishTicks.get(0));
            if (ps != null && !ps.isEmpty()) {
                String firstP = ps.get(0);
                Matcher m = Pattern.compile("new P\\(([\\d\\.-]+)f, ([\\d\\.-]+)f, ([\\d\\.-]+)f").matcher(firstP);
                if (m.find()) {
                    finishAnchor = new double[]{Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))};
                }
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("package org.dimasik.shame.modules.impl;\n\n");
        out.append("import java.util.*;\n\n");
        out.append("public class ParticleDataStorage {\n");
        out.append("    public static class P { public float x, y, z; public int color; public P(float x, float y, float z, int color) { this.x=x; this.y=y; this.z=z; this.color=color; } }\n");
        out.append("    public static class S { public String sound; public float vol, pitch; public S(String sound, float vol, float pitch) { this.sound=sound; this.vol=vol; this.pitch=pitch; } }\n");
        out.append("    public static class Frame { public int tick; public P[] particles; public S[] sounds; public Frame(int tick, P[] particles, S[] sounds) { this.tick=tick; this.particles=particles; this.sounds=sounds; } }\n\n");

        writeFrames(out, "startAnimation", startTicks, startBase, startAnchor, particles, sounds);
        out.append("\n");
        writeFrames(out, "finishAnimation", finishTicks, finishBase, finishAnchor, particles, sounds);
        out.append("}\n");

        Files.write(Paths.get("src/main/java/org/dimasik/shame/modules/impl/ParticleDataStorage.java"), out.toString().getBytes());
    }

    private static Map<Integer, List<String>> parseSection(String content, String name) {
        Map<Integer, List<String>> res = new HashMap<>();
        Matcher sectionMatcher = Pattern.compile(name + ":(.*?)(?=\\n\\w+:|$)", Pattern.DOTALL).matcher(content);
        if (!sectionMatcher.find()) return res;
        
        String sectionContent = sectionMatcher.group(1);
        String[] blocks = sectionContent.split("\\n  (-?\\d+):");
        
        Matcher tickFinder = Pattern.compile("\\n  (-?\\d+):").matcher(sectionContent);
        int blockIdx = 1;
        while (tickFinder.find()) {
            int tick = Integer.parseInt(tickFinder.group(1));
            String block = blocks[blockIdx++];
            List<String> items = new ArrayList<>();
            
            if (name.equals("particles")) {
                Matcher pm = Pattern.compile("- type: ([\\w:]+).*?pos:\\s*\\n\\s*-\\s*([\\d\\.-]+)\\s*\\n\\s*-\\s*([\\d\\.-]+)\\s*\\n\\s*-\\s*([\\d\\.-]+)(.*?)(?=- type:|$)", Pattern.DOTALL).matcher(block);
                while (pm.find()) {
                    String type = pm.group(1);
                    double x = Double.parseDouble(pm.group(2));
                    double y = Double.parseDouble(pm.group(3));
                    double z = Double.parseDouble(pm.group(4));
                    String extra = pm.group(5);
                    int color = -1;
                    if (extra.contains("color: '")) {
                        String hex = extra.split("color: '")[1].split("'")[0].replace("#", "");
                        color = Integer.parseInt(hex, 16);
                    }
                    items.add(String.format(Locale.US, "new P(%ff, %ff, %ff, 0x%X)", x, y, z, color));
                }
            } else {
                Matcher sm = Pattern.compile("- sound: ([\\w:\\.]+).*?volume: ([\\d\\.-]+).*?pitch: ([\\d\\.-]+)", Pattern.DOTALL).matcher(block);
                while (sm.find()) {
                    String sname = sm.group(1).replace("minecraft:", "").toUpperCase().replace(".", "_");
                    float vol = Float.parseFloat(sm.group(2));
                    float pitch = Float.parseFloat(sm.group(3));
                    items.add(String.format(Locale.US, "new S(\"%s\", %ff, %ff)", sname, vol, pitch));
                }
            }
            if (!items.isEmpty()) res.put(tick, items);
        }
        return res;
    }

    private static void writeFrames(StringBuilder out, String name, List<Integer> ticks, int base, double[] anchor, Map<Integer, List<String>> particles, Map<Integer, List<String>> sounds) {
        out.append("    public static final Frame[] ").append(name).append(" = new Frame[] {\n");
        for (int t : ticks) {
            int relTick = t - base;
            List<String> ps = particles.getOrDefault(t, new ArrayList<>());
            List<String> ss = sounds.getOrDefault(t, new ArrayList<>());
            
            out.append("        new Frame(").append(relTick).append(", ");
            if (ps.isEmpty()) out.append("new P[0]");
            else {
                out.append("new P[] {");
                for (int i = 0; i < ps.size(); i++) {
                    String pStr = ps.get(i);
                    // Adjust coordinates
                    Matcher m = Pattern.compile("new P\\(([\\d\\.-]+)f, ([\\d\\.-]+)f, ([\\d\\.-]+)f, (0x[0-9A-F]+)\\)").matcher(pStr);
                    if (m.find()) {
                        double x = Double.parseDouble(m.group(1)) - anchor[0];
                        double y = Double.parseDouble(m.group(2)) - anchor[1];
                        double z = Double.parseDouble(m.group(3)) - anchor[2];
                        out.append(String.format(Locale.US, "new P(%.3ff, %.3ff, %.3ff, %s)", x, y, z, m.group(4)));
                    }
                    if (i < ps.size() - 1) out.append(", ");
                }
                out.append("}");
            }
            out.append(", ");
            if (ss.isEmpty()) out.append("new S[0]");
            else {
                out.append("new S[] {");
                for (int i = 0; i < ss.size(); i++) {
                    out.append(ss.get(i));
                    if (i < ss.size() - 1) out.append(", ");
                }
                out.append("}");
            }
            out.append("),\n");
        }
        out.append("    };\n");
    }
}

