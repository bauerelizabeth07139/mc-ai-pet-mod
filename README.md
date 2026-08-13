# MC AI Pet Mod

Minecraft Forge 模组，让 AI 控制 FakePlayer 模拟真实玩家行为。宠物拥有完整的玩家数值和操作能力，可用于单人游戏模拟联机体验，或在多人服务器中增加"虚拟玩家"数量。

## 核心特性

### 与真实玩家一致的行为系统
- **移动**：使用 `Navigation.moveTo()` 模拟真实玩家行走路径规划和速度
- **挖掘**：使用 `level.destroyBlock()` 模拟真实挖掘速度、动画和掉落
- **放置**：使用 `level.setBlockAndUpdate()` 模拟真实方块放置
- **攻击**：使用 `player.attack()` 模拟真实攻击冷却、距离判定和伤害
- **进食**：使用 `player.eat()` 模拟真实进食动画和饥饿恢复
- **受伤**：完整继承真实玩家的受伤动画、无敌帧和死亡机制

### 智能优先级任务系统
基于 `PetTaskSystem` 实现动态优先级调度，宠物会根据当前状态自主决策：

```
食物(饥饿<6) > 安全(生命<50%/遇敌) > 合成(无基础工具) > 挖矿(缺资源) > 探索 > 空闲 wandering
```

每个任务都有独立的 `canRun()`, `tick()`, `isComplete()` 生命周期，可扩展新任务类型。

### AI 行为模式
- **生存模式 (SURVIVAL)**: 自动管理饥饿值、躲避危险、挖矿、合成工具、探索世界
- **建造模式 (BUILDING)**: 自动收集材料并建造预设建筑模板
- **护卫模式 (GUARD)**: 跟随主人，扫描并攻击周围敌人，保护主人安全
- **扫荡模式 (SWEEP)**: 在指定范围内自动搜寻并攻击敌对生物

### 皮肤系统
- 宠物生成时自动继承最近真实玩家的皮肤（GameProfile）
- 支持实时同步：主人更换皮肤后，宠物会自动更新外观
- 宠物在 Tab 列表和排行榜中显示正常

### 数据持久化
- 宠物数据自动保存到 `config/mcaipet.json`
- 宠物状态（位置、模式、生命值、任务状态）持久化存储
- 服务器重启后自动恢复所有宠物及其皮肤

### 建筑模板
宠物在建造模式下可以建造以下 9 种建筑：
- 小屋 (small_house)
- 高塔 (tower)
- 墙壁 (wall)
- 桥梁 (bridge)
- 农田 (farm)
- 路灯 (lamp_post)
- 储物箱 (storage)
- 楼梯 (stairs)
- 平台 (platform)

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/aipet spawn <count>` | 生成指定数量的 AI 宠物（1-50） | 管理员 |
| `/aipet mode <mode>` | 切换宠物模式：`survival` / `building` / `guard` / `sweep` | 管理员 |
| `/aipet follow <player>` | 让护卫模式宠物跟随指定玩家 | 管理员 |
| `/aipet stop` | 停止所有宠物的 AI | 管理员 |
| `/aipet dismiss` | 解散所有 AI 宠物 | 管理员 |
| `/aipet count` | 查看当前宠物数量及详细信息 | 管理员 |

## 安装

1. 安装 Minecraft Forge（推荐 1.20.1）
2. 下载模组 jar 文件
3. 将 jar 文件放入 `.minecraft/mods/` 文件夹
4. 启动游戏即可

## 配置

模组会在 `config/mcaipet.json` 中生成配置文件：

```json
{
  "defaultPetCount": 3,
  "maxPetCount": 20,
  "guardRange": 200,
  "sweepRange": 50,
  "petSpeed": 1.0,
  "aiReach": 4.0,
  "buildTickInterval": 100
}
```

## 依赖

- Minecraft 1.20.1
- Forge 47.2.0+
- Java 17+

## 适用场景

- **单人游戏**：模拟多人联机体验，让 AI 玩家陪伴冒险
- **多人服务器**：增加在线玩家数量，填充服务器人口
- **娱乐演示**：观看 AI 玩家自主生存、建造、战斗