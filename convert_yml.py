import re
import sys

def parse_shame_yml(file_path):
    with open(file_path, 'r') as f:
        content = f.read()
    
    start_pos_match = re.search(r"start_position:\s*\n-\s*([\d\.-]+)\s*\n-\s*([\d\.-]+)\s*\n-\s*([\d\.-]+)", content)
    start_pos = [float(x) for x in start_pos_match.groups()] if start_pos_match else [0, 0, 0]
    
    def parse_section(section_name):
        section_match = re.search(rf"{section_name}:(.*?)(?=\n\w+:|$)", content, re.DOTALL)
        if not section_match:
            return {}
        
        section_content = section_match.group(1)
        entries = {}
        
        # Find ticks: "  -675:"
        tick_blocks = re.split(r"\n  (-?\d+):", section_content)
        for i in range(1, len(tick_blocks), 2):
            tick = int(tick_blocks[i])
            block = tick_blocks[i+1]
            
            items = []
            # For particles: "- type: ... pos: [x,y,z] ... extra: {color: '#...'}"
            if section_name == 'particles':
                p_matches = re.finditer(r"- type: ([\w:]+).*?pos:\s*\n\s*-\s*([\d\.-]+)\s*\n\s*-\s*([\d\.-]+)\s*\n\s*-\s*([\d\.-]+).*?color: '(#\w+)'", block, re.DOTALL)
                for pm in p_matches:
                    items.append({
                        'pos': [float(pm.group(2)), float(pm.group(3)), float(pm.group(4))],
                        'extra': {'color': pm.group(5)}
                    })
                # Also match fireworks which might not have color
                f_matches = re.finditer(r"- type: ([\w:]+).*?pos:\s*\n\s*-\s*([\d\.-]+)\s*\n\s*-\s*([\d\.-]+)\s*\n\s*-\s*([\d\.-]+)(?!\s*extra:)", block, re.DOTALL)
                for fm in f_matches:
                    if fm.group(1) == 'minecraft:firework':
                        items.append({
                            'pos': [float(fm.group(2)), float(fm.group(3)), float(fm.group(4))],
                            'type': fm.group(1)
                        })

            # For sounds: "- sound: ... volume: ... pitch: ..."
            elif section_name == 'sounds':
                s_matches = re.finditer(r"- sound: ([\w:\.]+).*?volume: ([\d\.-]+).*?pitch: ([\d\.-]+)", block, re.DOTALL)
                for sm in s_matches:
                    items.append({
                        'sound': sm.group(1),
                        'volume': float(sm.group(2)),
                        'pitch': float(sm.group(3))
                    })
            
            if items:
                entries[tick] = items
        return entries

    particles = parse_section('particles')
    sounds = parse_section('sounds')
    
    all_ticks = sorted(set(list(particles.keys()) + list(sounds.keys())))
    
    start_ticks = [t for t in all_ticks if t < 0]
    finish_ticks = [t for t in all_ticks if t > 1000]
    
    if not start_ticks:
        return None, None
        
    first_tick = start_ticks[0]
    
    # Use the first particle of the first tick as anchor
    anchor = start_pos
    if first_tick in particles and particles[first_tick]:
        anchor = particles[first_tick][0]['pos']

    def format_frame(ticks_list, base_tick, anchor_pos):
        frames = []
        for t in ticks_list:
            relative_tick = t - base_tick
            frame_particles = particles.get(t, [])
            frame_sounds = sounds.get(t, [])
            
            p_data = []
            for p in frame_particles:
                rel_x = p['pos'][0] - anchor_pos[0]
                rel_y = p['pos'][1] - anchor_pos[1]
                rel_z = p['pos'][2] - anchor_pos[2]
                color = p.get('extra', {}).get('color', '#FFFFFF').replace('#', '')
                if 'type' in p and p['type'] == 'minecraft:firework':
                    p_data.append(f"new P({rel_x:.3f}f, {rel_y:.3f}f, {rel_z:.3f}f, -1)")
                else:
                    p_data.append(f"new P({rel_x:.3f}f, {rel_y:.3f}f, {rel_z:.3f}f, 0x{color})")
            
            s_data = []
            for s in frame_sounds:
                name = s['sound'].replace('minecraft:', '').upper().replace('.', '_')
                vol = s['volume']
                pitch = s['pitch']
                s_data.append(f"new S(\"{name}\", {vol}f, {pitch}f)")
            
            if p_data or s_data:
                frames.append((relative_tick, p_data, s_data))
        return frames

    start_frames = format_frame(start_ticks, first_tick, anchor)
    
    finish_anchor = anchor
    if finish_ticks and finish_ticks[0] in particles and particles[finish_ticks[0]]:
        finish_anchor = particles[finish_ticks[0]][0]['pos']
    
    finish_frames = format_frame(finish_ticks, finish_ticks[0] if finish_ticks else 0, finish_anchor)
    
    return start_frames, finish_frames

def generate_java(start_frames, finish_frames):
    out = []
    out.append("package org.dimasik.shame.modules.impl;")
    out.append("")
    out.append("import java.util.*;")
    out.append("")
    out.append("public class ParticleDataStorage {")
    out.append("    public static class P { public float x, y, z; public int color; public P(float x, float y, float z, int color) { this.x=x; this.y=y; this.z=z; this.color=color; } }")
    out.append("    public static class S { public String sound; public float vol, pitch; public S(String sound, float vol, float pitch) { this.sound=sound; this.vol=vol; this.pitch=pitch; } }")
    out.append("    public static class Frame { public int tick; public P[] particles; public S[] sounds; public Frame(int tick, P[] particles, S[] sounds) { this.tick=tick; this.particles=particles; this.sounds=sounds; } }")
    out.append("")
    
    def write_frames(name, frames):
        out.append(f"    public static final Frame[] {name} = new Frame[] {{")
        for tick, p_list, s_list in frames:
            p_str = "new P[] {" + ", ".join(p_list) + "}" if p_list else "new P[0]"
            s_str = "new S[] {" + ", ".join(s_list) + "}" if s_list else "new S[0]"
            out.append(f"        new Frame({tick}, {p_str}, {s_str}),")
        out.append("    };")

    write_frames("startAnimation", start_frames)
    out.append("")
    write_frames("finishAnimation", finish_frames)
    
    out.append("}")
    return "\n".join(out)

if __name__ == "__main__":
    s, f = parse_shame_yml('shame.yml')
    java_code = generate_java(s, f)
    with open('src/main/java/org/dimasik/shame/modules/impl/ParticleDataStorage.java', 'w') as f:
        f.write(java_code)

