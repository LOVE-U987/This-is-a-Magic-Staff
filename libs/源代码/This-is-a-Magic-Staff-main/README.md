<!-- 
This is a Magic Staff! / 万法皆杖
Transform any item into a spellcasting implement
-->

<div align="center">

<img src="https://img.shields.io/badge/Minecraft-1.21.1-5D8736?style=for-the-badge&logo=minecraft&logoColor=white">
<img src="https://img.shields.io/badge/NeoForge-21.1.219+-E04E14?style=for-the-badge">
<img src="https://img.shields.io/badge/Requires-ISS-8A2BE2?style=for-the-badge">

<br><br>

<pre>
██╗  ██╗██╗    ██╗███████╗██╗      █████╗ ███╗   ███╗ █████╗ ██╗  ██╗ ██████╗ ███████╗████████╗
██║  ██║██║    ██║██╔════╝██║     ██╔══██╗████╗ ████║██╔══██╗██║ ██╔╝██╔════╝ ██╔════╝╚══██╔══╝
███████║██║ █╗ ██║███████╗██║     ███████║██╔████╔██║███████║█████╔╝ ██║  ███╗█████╗     ██║   
██╔══██║██║███╗██║╚════██║██║     ██╔══██║██║╚██╔╝██║██╔══██║██╔═██╗ ██║   ██║██╔══╝     ██║   
██║  ██║╚███╔███╔╝███████║███████╗██║  ██║██║ ╚═╝ ██║██║  ██║██║  ██╗╚██████╔╝███████╗   ██║   
╚═╝  ╚═╝ ╚══╝╚══╝ ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝   ╚═╝   
                                                                                                
██████╗  █████╗ ████████╗██╗███████╗███╗   ██╗████████╗    ███████╗██╗  ██╗ ██████╗  ██████╗██╗  ██╗
██╔══██╗██╔══██╗╚══██╔══╝██║██╔════╝████╗  ██║╚══██╔══╝    ██╔════╝██║  ██║██╔═══██╗██╔════╝██║ ██╔╝
██║  ██║███████║   ██║   ██║█████╗  ██╔██╗ ██║   ██║       ███████╗███████║██║   ██║██║     █████╔╝ 
██║  ██║██╔══██║   ██║   ██║██╔══╝  ██║╚██╗██║   ██║       ╚════██║██╔══██║██║   ██║██║     ██╔═██╗ 
██████╔╝██║  ██║   ██║   ██║███████╗██║ ╚████║   ██║       ███████║██║  ██║╚██████╔╝╚██████╗██║  ██╗
╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚═╝╚══════╝╚═╝  ╚═══╝   ╚═╝       ╚══════╝╚═╝  ╚═╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝
</pre>

<h1>🪄 This is a Magic Staff! <span style="font-size: 0.6em; opacity: 0.8;">万法皆杖</span></h1>

<p><i>Turn any item into a spellcasting implement — Your sword is now your wand</i></p>

<img src="https://img.shields.io/badge/🔮_Dynamic_Conversion-9B59B6?style=flat-square">
<img src="https://img.shields.io/badge/⚔️_Weapon_&_Staff-3498DB?style=flat-square">
<img src="https://img.shields.io/badge/💾_State_Persistent-2ECC71?style=flat-square">

</div>

---

## 📖 Overview / 简介

**This is a Magic Staff!** (万法皆杖) is a **NeoForge 1.21.1** addon for **[Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks)** that liberates your spellcasting potential.

Transform **any item** in your hand — diamond swords, iron axes, even a potato — into a functional spellcasting staff with a single keystroke. Cast devastating spells, then revert back to melee combat instantly.

```diff
+ No crafting recipes needed — use existing gear
+ Seamless weapon/staff switching mid-combat
+ Persistent state saved to item NBT
+ Zero visual clutter — no new item sprites
```

---

## ✨ Key Features / 核心功能

<div align="center">

| Feature | Description | Benefit |
|:---:|:---|:---|
| 🔮 **Dynamic Conversion** | Toggle any item's casting capability on/off | No inventory bloat |
| ⚔️ **Dual-Mode Items** | Switch between melee weapon and spell staff | Tactical flexibility |
| 💾 **Persistent State** | Conversion status saved in item data | Survives drops/death |
| 🌐 **Bilingual** | Full English & 简体中文 support | Global accessibility |

</div>

---

## 🎮 How to Use / 使用方法

<div align="center">

```
Step 1: Hold any item (Sword, Axe, anything!)
    ↓
Step 2: Press [H] to activate casting mode
    ↓
Step 3: Equip spells via ISS spellbook/wheel
    ↓
Step 4: Right-click to cast!
    ↓
Step 5: Press [H] again to revert to weapon
```

</div>

> 💡 **Tip**: Keybind can be customized in Options → Controls → Key Binds

---

## 🔧 Technical Details / 技术原理

Utilizes Minecraft 1.21.1's **Data Component System**:

```java
// Core logic (simplified)
item.set(DataComponents.CASTING_IMPLEMENT, true);  // Enable casting
item.remove(DataComponents.CASTING_IMPLEMENT);      // Disable casting
```

Iron's Spells 'n Spellbooks automatically recognizes items with the `CASTING_IMPLEMENT` component, enabling right-click spellcasting functionality.

---

## 📋 Requirements / 前置要求

<div align="center">

| Component | Version | Required |
|:---:|:---:|:---:|
| **Minecraft** | 1.21.1 | ✅ |
| **NeoForge** | 21.1.219+ | ✅ |
| **Iron's Spells 'n Spellbooks** | 3.15.0+ | ✅ |
| Curios API | Continuation | ✅ (ISS Dependency) |
| GeckoLib | 4.x | ✅ (ISS Dependency) |
| Player Animation Lib | Latest | ✅ (ISS Dependency) |

</div>

---

## 📝 Configuration / 配置说明

**Config Location**: `.minecraft/config/this_is_a_magic_staff-common.toml`

```toml
[magic]
# Enable debug logging
logDebug = false
```

---

## 🐛 Known Issues / 已知问题

- None currently reported

Please submit issues via [GitHub Issues](https://github.com) or CurseForge comments!

---

<div align="center">

## 📥 Download / 下载

[![CurseForge](https://img.shields.io/badge/CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://curseforge.com)
[![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com)

*✨🪄Every item is a wand waiting to be awakened.🪄✨*

</div>
