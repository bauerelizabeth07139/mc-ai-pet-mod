# MC AI Pet Mod

Minecraft Forge 模组，为宠物添加智能行为系统。宠物会根据当前模式自主决策并执行行动，包括生存、建造、护卫和扫荡。

## 特性

### AI 行为模式
- **生存模式 (SURVIVAL)**: 自动管理饥饿值、躲避危险、挖矿、合成工具、探索世界
- **建造模式 (BUILDING)**: 自动收集材料并建造预设建筑模板
- **护卫模式 (GUARD)**: 跟随主人，扫描并攻击周围敌人，保护主人安全
- **扫荡模式 (SWEEP)**: 在指定范围内自动搜寻并攻击敌对生物

### 配置系统
- 默认宠物数量：3
- 最大宠物数量：20
- 护卫范围：200 格
- 扫荡范围：50 格
- 宠物移动速度：1.0x
- AI 攻击距离：4.0 格
- 建造间隔：100 ticks

### 数据持久化
- 宠物数据自动保存到 `config/mcaipet.json`
- 宠物状态（位置、模式、生命值、黑板数据）持久化存储
- 服务器重启后自动恢复所有宠物

### 建筑模板
宠物在建造模式下可以建造以下建筑：
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

模组会在 `config/mcaipet.json` 中生成配置文件，可以在游戏中或手动修改：

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