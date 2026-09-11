using System.Collections.Generic;
using HourglassPalace;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.Rendering.Universal;
using UnityEngine.UI;

namespace HourglassPalace.EditorTools
{
    /// <summary>
    /// Builds the Hall of Falling Sands from code so the whole slice - twelve
    /// screens, their traps, guards, lanterns and the moon gate - is one
    /// reviewable, re-runnable description rather than a scene nobody can diff.
    /// </summary>
    public static class PlayableSliceBuilder
    {
        const string ScenePath = "Assets/Scenes/HallOfFallingSands.unity";

        static int _solidLayer;
        static Sprite _sandstone, _sandstoneDark, _pillar, _moonstone, _bronzeDoor, _spikeTile;
        static Sprite _lanternSprite, _milkSprite, _plateSprite, _leverSprite, _chainSprite;
        static Sprite _sawSprite, _spikeBedSprite, _brickSprite, _moonKeySprite, _scimitarSprite;
        static Sprite _sandBladeSprite;
        static Transform _world;

        [MenuItem("Hourglass Palace/Build Playable Slice")]
        public static void Build()
        {
            PlayerSettings.companyName = "Meowa2D";
            PlayerSettings.productName = "喵白白：沙漏宮";
            QualitySettings.antiAliasing = 0;
            QualitySettings.anisotropicFiltering = AnisotropicFiltering.Disable;

            _solidLayer = ArtPipeline.EnsureLayer("Solid");
            ArtPipeline.ReimportAll();
            LoadSprites();

            var scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);
            scene.name = "HallOfFallingSands";
            _world = new GameObject("World").transform;

            var camera = BuildCamera();
            BuildBackdrop(camera.transform);

            var player = BuildPlayer();
            camera.GetComponent<CameraDirector>().target = player.transform;

            var director = new GameObject("GameDirector").AddComponent<GameDirector>();
            director.player = player;
            director.hourglassSeconds = 900f;
            director.sealRetrySeconds = 300f;
            director.gameObject.AddComponent<KillPlane>().y = -26f;

            var lanterns = new List<LanternCheckpoint>();
            BuildScreens(lanterns, director);
            director.startLantern = lanterns.Count > 0 ? lanterns[0] : null;

            BuildHud(director);

            EnsureFolder("Assets/Scenes");
            EditorSceneManager.MarkSceneDirty(scene);
            EditorSceneManager.SaveScene(scene, ScenePath);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(ScenePath, true) };
            AssetDatabase.SaveAssets();
            Debug.Log("[PlayableSliceBuilder] Hall of Falling Sands built: 12 screens, 12 lanterns, moon gate.");
        }

        public static void BuildAndExit()
        {
            Build();
            EditorApplication.Exit(0);
        }

        static void LoadSprites()
        {
            _sandstone = ArtPipeline.Load("Assets/Art/Tiles/sandstone.png");
            _sandstoneDark = ArtPipeline.Load("Assets/Art/Tiles/sandstone_dark.png");
            _pillar = ArtPipeline.LoadAny("Assets/Art/Tiles/pillar.png", "Assets/Art/Tiles/sandstone_dark.png");
            _moonstone = ArtPipeline.LoadAny("Assets/Art/Tiles/moonstone.png", "Assets/Art/Tiles/teal.png");
            _bronzeDoor = ArtPipeline.LoadAny("Assets/Art/Tiles/bronze_door.png", "Assets/Art/Tiles/sandstone_dark.png");
            _spikeTile = ArtPipeline.Load("Assets/Art/Tiles/spike.png");
            _sandBladeSprite = ArtPipeline.LoadAny("Assets/Art/Tiles/sand_blade.png", "Assets/Art/Tiles/teal.png");

            _spikeBedSprite = ArtPipeline.LoadAny("Assets/Art/Props/Sliced/trap_00.png", "Assets/Art/Tiles/spike.png");
            _leverSprite = ArtPipeline.Load("Assets/Art/Props/Sliced/trap_01.png");
            _milkSprite = ArtPipeline.Load("Assets/Art/Props/Sliced/trap_02.png");
            _plateSprite = ArtPipeline.Load("Assets/Art/Props/Sliced/trap_03.png");
            _chainSprite = ArtPipeline.LoadAny("Assets/Art/Props/Sliced/trap_04.png", "Assets/Art/Props/Sliced/trap_08.png");
            _sawSprite = ArtPipeline.LoadAny("Assets/Art/Props/Sliced/trap_05.png", "Assets/Art/Props/Sliced/trap_09.png");
            _lanternSprite = ArtPipeline.LoadAny("Assets/Art/Props/Sliced/trap_06.png", "Assets/Art/Props/Sliced/trap_10.png");
            _brickSprite = ArtPipeline.LoadAny("Assets/Art/Props/Sliced/trap_07.png", "Assets/Art/Tiles/sandstone_dark.png");
            _moonKeySprite = ArtPipeline.LoadAny("Assets/Art/Props/Loot/loot_02.png", "Assets/Art/Tiles/teal.png");
            _scimitarSprite = ArtPipeline.LoadAny("Assets/Art/Props/Loot/loot_00.png", "Assets/Art/Tiles/teal.png");
        }

        // ---- rig ------------------------------------------------------------

        static Camera BuildCamera()
        {
            var go = new GameObject("Main Camera") { tag = "MainCamera" };
            var cam = go.AddComponent<Camera>();
            cam.orthographic = true;
            cam.orthographicSize = (ArtPipeline.RefY / 2f) / ArtPipeline.TilePpu;
            cam.clearFlags = CameraClearFlags.SolidColor;
            cam.backgroundColor = new Color(0.055f, 0.045f, 0.085f, 1f);
            cam.allowHDR = false;
            cam.allowMSAA = false;
            go.transform.position = new Vector3(0f, 0f, -10f);
            go.AddComponent<AudioListener>();
            if (go.GetComponent<UniversalAdditionalCameraData>() == null)
                go.AddComponent<UniversalAdditionalCameraData>();

            var pp = go.AddComponent<PixelPerfectCamera>();
            pp.assetsPPU = ArtPipeline.TilePpu;
            pp.refResolutionX = ArtPipeline.RefX;
            pp.refResolutionY = ArtPipeline.RefY;
            pp.gridSnapping = PixelPerfectCamera.GridSnapping.PixelSnapping;
            pp.cropFrame = PixelPerfectCamera.CropFrame.None;

            go.AddComponent<CameraDirector>();

            var lightGo = new GameObject("Global Light 2D");
            var light = lightGo.AddComponent<Light2D>();
            light.lightType = Light2D.LightType.Global;
            light.intensity = 1.0f;
            light.color = new Color(1f, 0.95f, 0.86f);
            return cam;
        }

        static void BuildBackdrop(Transform cam)
        {
            void Band(string name, string path, float y, int order, Vector2 factor, Color tint)
            {
                var sprite = ArtPipeline.Load(path);
                if (sprite == null) return;
                var go = new GameObject(name);
                go.transform.SetParent(_world, false);
                go.transform.position = new Vector3(120f, y, 0f);
                var sr = go.AddComponent<SpriteRenderer>();
                sr.sprite = sprite;
                sr.drawMode = SpriteDrawMode.Tiled;
                sr.tileMode = SpriteTileMode.Continuous;
                sr.size = new Vector2(360f, 16f);
                sr.sortingOrder = order;
                sr.color = tint;
                var layer = go.AddComponent<ParallaxLayer>();
                layer.cam = cam;
                layer.factor = factor;
            }

            // Two stacked bands cover the upper halls and the shafts below them.
            Band("Background_Upper", "Assets/Art/Map/background.png", 3f, -40, new Vector2(0.14f, 0.06f), new Color(0.72f, 0.7f, 0.8f));
            Band("Background_Lower", "Assets/Art/Map/background.png", -13f, -40, new Vector2(0.14f, 0.06f), new Color(0.6f, 0.58f, 0.7f));
            Band("Midground_Upper", "Assets/Art/Map/midground.png", 2f, -30, new Vector2(0.32f, 0.14f), new Color(0.86f, 0.83f, 0.88f));
            Band("Midground_Lower", "Assets/Art/Map/midground.png", -14f, -30, new Vector2(0.32f, 0.14f), new Color(0.74f, 0.71f, 0.8f));
            Band("Foreground_Upper", "Assets/Art/Map/foreground.png", 1f, -20, new Vector2(0.55f, 0.24f), new Color(1f, 0.98f, 0.94f, 0.9f));
            Band("Foreground_Lower", "Assets/Art/Map/foreground.png", -15f, -20, new Vector2(0.55f, 0.24f), new Color(0.9f, 0.88f, 0.86f, 0.9f));
        }

        static PlayerController BuildPlayer()
        {
            var go = new GameObject("Miaobaibai") { tag = "Player" };
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3(2f, 0.1f, 0f);

            var rb = go.AddComponent<Rigidbody2D>();
            rb.gravityScale = 4.6f;
            rb.freezeRotation = true;
            rb.collisionDetectionMode = CollisionDetectionMode2D.Continuous;
            rb.interpolation = RigidbodyInterpolation2D.Interpolate;

            var capsule = go.AddComponent<CapsuleCollider2D>();
            capsule.direction = CapsuleDirection2D.Vertical;
            capsule.size = new Vector2(0.9f, 1.9f);
            capsule.offset = new Vector2(0f, 0.95f);

            var player = go.AddComponent<PlayerController>();
            player.solidMask = 1 << _solidLayer;

            var art = new GameObject("Sprite");
            art.transform.SetParent(go.transform, false);
            var sr = art.AddComponent<SpriteRenderer>();
            sr.sortingOrder = 14;

            var flashGo = new GameObject("BladeFlash");
            flashGo.transform.SetParent(art.transform, false);
            flashGo.transform.localPosition = new Vector3(0.95f, 1.0f, 0f);
            var flash = flashGo.AddComponent<SpriteRenderer>();
            flash.sprite = _scimitarSprite;
            flash.sortingOrder = 15;
            flash.color = new Color(1f, 0.97f, 0.8f, 0.95f);
            flash.enabled = false;

            var anim = art.AddComponent<PlayerAnimator>();
            anim.player = player;
            anim.idleFrames = ArtPipeline.LoadFrames("Assets/Art/Hero/miaobaibai_idle_strip.png");
            anim.walkFrames = ArtPipeline.LoadFrames("Assets/Art/Hero/miaobaibai_walk_strip.png");
            anim.bladeFlash = flash;
            if (anim.idleFrames.Length == 0)
                anim.idleFrames = new[] { ArtPipeline.Load("Assets/Art/Hero/miaobaibai.png") };
            if (anim.walkFrames.Length == 0) anim.walkFrames = anim.idleFrames;
            sr.sprite = anim.idleFrames.Length > 0 ? anim.idleFrames[0] : null;

            return player;
        }

        // ---- building blocks ---------------------------------------------------

        static GameObject Slab(string name, float xMin, float xMax, float topY, float thickness,
            Sprite sprite, int order = 0)
        {
            var go = new GameObject(name) { layer = _solidLayer };
            go.transform.SetParent(_world, false);
            float w = xMax - xMin;
            go.transform.position = new Vector3(xMin + w * 0.5f, topY - thickness * 0.5f, 0f);

            var sr = go.AddComponent<SpriteRenderer>();
            sr.sprite = sprite;
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(w, thickness);
            sr.sortingOrder = order;

            var box = go.AddComponent<BoxCollider2D>();
            box.size = new Vector2(w, thickness);
            return go;
        }

        static GameObject Prop(string name, Sprite sprite, Vector3 pos, int order = 4, Transform parent = null)
        {
            var go = new GameObject(name);
            go.transform.SetParent(parent != null ? parent : _world, false);
            go.transform.position = pos;
            var sr = go.AddComponent<SpriteRenderer>();
            sr.sprite = sprite;
            sr.sortingOrder = order;
            return go;
        }

        static BoxCollider2D TriggerBox(GameObject go, Vector2 size, Vector2 offset)
        {
            var box = go.AddComponent<BoxCollider2D>();
            box.isTrigger = true;
            box.size = size;
            box.offset = offset;
            return box;
        }

        static LevelRoom Room(int index, string screenName, float xMin, float xMax, float yMin, float yMax)
        {
            var go = new GameObject($"Room_{index:00}_{screenName}");
            go.transform.SetParent(_world, false);
            var room = go.AddComponent<LevelRoom>();
            room.index = index;
            room.screenName = screenName;
            room.bounds = new Rect(xMin, yMin, xMax - xMin, yMax - yMin);
            go.transform.position = room.bounds.center;
            return room;
        }

        static LanternCheckpoint Lantern(List<LanternCheckpoint> all, int index, string screenName,
            float x, float topY, bool lit = false)
        {
            var go = Prop($"Lantern_{index:00}", _lanternSprite, new Vector3(x, topY, 0f), 5);
            TriggerBox(go, new Vector2(1.1f, 1.5f), new Vector2(0f, 0.7f));
            var lantern = go.AddComponent<LanternCheckpoint>();
            lantern.index = index;
            lantern.screenName = screenName;
            lantern.litAtStart = lit;
            lantern.flame = go.GetComponent<SpriteRenderer>();
            lantern.spawnOffset = new Vector3(0.6f, 0.1f, 0f);

            var glowGo = new GameObject("Glow");
            glowGo.transform.SetParent(go.transform, false);
            glowGo.transform.localPosition = new Vector3(0f, 0.75f, 0f);
            var glow = glowGo.AddComponent<Light2D>();
            glow.lightType = Light2D.LightType.Point;
            glow.pointLightOuterRadius = 5.5f;
            glow.pointLightInnerRadius = 0.6f;
            glow.color = new Color(1f, 0.78f, 0.45f);
            glow.intensity = lit ? 1.15f : 0f;
            lantern.glow = glow;

            all.Add(lantern);
            return lantern;
        }

        static SpikeFloor Spikes(string name, float xMin, float xMax, float topY, float phase)
        {
            var go = new GameObject(name);
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3((xMin + xMax) * 0.5f, topY, 0f);

            float w = xMax - xMin;
            var art = new GameObject("Blades");
            art.transform.SetParent(go.transform, false);
            var sr = art.AddComponent<SpriteRenderer>();
            sr.sprite = _spikeBedSprite;
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(w, 1f);
            sr.sortingOrder = 6;
            art.transform.localPosition = new Vector3(0f, 0f, 0f);

            var box = go.AddComponent<BoxCollider2D>();
            box.isTrigger = true;
            box.size = new Vector2(w * 0.92f, 0.75f);
            box.offset = new Vector2(0f, 0.45f);

            var spikes = go.AddComponent<SpikeFloor>();
            spikes.phaseOffset = phase;
            spikes.bladeSprite = sr;
            spikes.hurtBox = box;
            return spikes;
        }

        static void FixedSpikePit(string name, float xMin, float xMax, float floorY, float depth)
        {
            var go = new GameObject(name);
            go.transform.SetParent(_world, false);
            float w = xMax - xMin;
            go.transform.position = new Vector3((xMin + xMax) * 0.5f, floorY, 0f);

            var art = new GameObject("Spikes");
            art.transform.SetParent(go.transform, false);
            var sr = art.AddComponent<SpriteRenderer>();
            sr.sprite = _spikeBedSprite;
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(w, 1f);
            sr.sortingOrder = 6;

            var box = go.AddComponent<BoxCollider2D>();
            box.isTrigger = true;
            box.size = new Vector2(w, depth);
            box.offset = new Vector2(0f, depth * 0.5f);
            go.AddComponent<LethalZone>().instantKill = true;
        }

        static CrumblingBrick Brick(string name, float centreX, float topY, float width = 1.8f)
        {
            var go = new GameObject(name) { layer = _solidLayer };
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3(centreX, topY - 0.25f, 0f);

            var sr = go.AddComponent<SpriteRenderer>();
            sr.sprite = _brickSprite;
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(width, 0.5f);
            sr.sortingOrder = 3;

            var box = go.AddComponent<BoxCollider2D>();
            box.size = new Vector2(width, 0.5f);
            var rb = go.AddComponent<Rigidbody2D>();
            rb.bodyType = RigidbodyType2D.Kinematic;
            rb.constraints = RigidbodyConstraints2D.FreezeRotation;
            return go.AddComponent<CrumblingBrick>();
        }

        static SawBlade Saw(string name, Vector2 at, Vector2 travel, float speed = 3.4f)
        {
            var go = Prop(name, _sawSprite, new Vector3(at.x, at.y, 0f), 7);
            var saw = go.AddComponent<SawBlade>();
            saw.travel = travel;
            saw.speed = speed;
            saw.hitRadius = 0.7f;
            return saw;
        }

        static ClimbChain Chain(string name, float x, float yBottom, float yTop)
        {
            var go = new GameObject(name);
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3(x, (yBottom + yTop) * 0.5f, 0f);

            var pivot = new GameObject("Links");
            pivot.transform.SetParent(go.transform, false);
            pivot.transform.localPosition = new Vector3(0f, (yTop - yBottom) * 0.5f, 0f);
            var sr = pivot.AddComponent<SpriteRenderer>();
            sr.sprite = _chainSprite;
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(0.35f, yTop - yBottom);
            sr.sortingOrder = 3;
            // pivot the sway from the ceiling anchor
            sr.transform.localPosition = new Vector3(0f, -(yTop - yBottom) * 0.5f, 0f);

            var box = go.AddComponent<BoxCollider2D>();
            box.isTrigger = true;
            box.size = new Vector2(0.85f, yTop - yBottom);

            var chain = go.AddComponent<ClimbChain>();
            chain.visual = pivot.transform;
            return chain;
        }

        static MechanismDoor Door(string name, float xMin, float xMax, float topY, float height,
            Vector2 openOffset, bool startOpen = false, bool openWhenUnpowered = false, Sprite sprite = null)
        {
            var go = Slab(name, xMin, xMax, topY, height, sprite != null ? sprite : _bronzeDoor, 2);
            var door = go.AddComponent<MechanismDoor>();
            door.openOffset = openOffset;
            door.startOpen = startOpen;
            door.openWhenUnpowered = openWhenUnpowered;
            return door;
        }

        static PressurePlate Plate(string name, float x, float topY, bool latching, params MonoBehaviour[] targets)
        {
            var go = Prop(name, _plateSprite, new Vector3(x, topY, 0f), 3);
            TriggerBox(go, new Vector2(1.9f, 0.6f), new Vector2(0f, 0.28f));
            var plate = go.AddComponent<PressurePlate>();
            plate.latching = latching;
            plate.plateVisual = go.transform;
            plate.targets = new List<MonoBehaviour>(targets);
            return plate;
        }

        static WallLever Lever(string name, float x, float topY, float holdSeconds, params MonoBehaviour[] targets)
        {
            var go = Prop(name, _leverSprite, new Vector3(x, topY, 0f), 5);
            TriggerBox(go, new Vector2(1.4f, 1.6f), new Vector2(0f, 0.8f));
            var lever = go.AddComponent<WallLever>();
            lever.holdSeconds = holdSeconds;
            lever.handle = go.transform;
            lever.targets = new List<MonoBehaviour>(targets);
            return lever;
        }

        static MilkBowl Milk(string name, float x, float topY)
        {
            var go = Prop(name, _milkSprite, new Vector3(x, topY, 0f), 4);
            TriggerBox(go, new Vector2(1.1f, 0.9f), new Vector2(0f, 0.4f));
            var bowl = go.AddComponent<MilkBowl>();
            bowl.bowl = go.GetComponent<SpriteRenderer>();
            return bowl;
        }

        static T Enemy<T>(string name, string spritePath, Vector3 at, Vector2 hitbox) where T : EnemyBase
        {
            var go = Prop(name, ArtPipeline.Load(spritePath), at, 11);
            var rb = go.AddComponent<Rigidbody2D>();
            rb.bodyType = RigidbodyType2D.Kinematic;
            rb.simulated = true;
            var box = go.AddComponent<BoxCollider2D>();
            box.isTrigger = true;
            box.size = hitbox;
            box.offset = new Vector2(0f, hitbox.y * 0.5f);
            return go.AddComponent<T>();
        }

        static Transform Marker(string name, Vector3 at)
        {
            var go = new GameObject(name);
            go.transform.SetParent(_world, false);
            go.transform.position = at;
            return go.transform;
        }

        static void Pillar(string name, float x, float bottomY, float topY)
        {
            var go = Prop(name, _pillar, new Vector3(x, bottomY, 0f), -5);
            var sr = go.GetComponent<SpriteRenderer>();
            sr.drawMode = SpriteDrawMode.Tiled;
            sr.tileMode = SpriteTileMode.Continuous;
            sr.size = new Vector2(1.4f, topY - bottomY);
            sr.color = new Color(0.72f, 0.6f, 0.48f);
            go.transform.position = new Vector3(x, (bottomY + topY) * 0.5f, 0f);
        }

        static void EnsureFolder(string path)
        {
            if (AssetDatabase.IsValidFolder(path)) return;
            var parent = System.IO.Path.GetDirectoryName(path)?.Replace('\\', '/');
            var leaf = System.IO.Path.GetFileName(path);
            AssetDatabase.CreateFolder(parent, leaf);
        }

        // ---- the twelve screens -------------------------------------------------

        static void BuildScreens(List<LanternCheckpoint> lanterns, GameDirector director)
        {
            int solidMask = 1 << _solidLayer;

            // 01 entrance: safe ground, the hourglass starts, nothing can kill you
            Room(1, "入口", -4f, 20f, -3f, 8.25f);
            Slab("Floor_Entrance", -4f, 20f, 0f, 1.4f, _sandstone);
            Slab("Step_Entrance", 12f, 16f, 1.4f, 1.4f, _sandstoneDark);
            Pillar("Pillar_01", 8f, 0f, 7f);
            Lantern(lanterns, 1, "入口", 3f, 0f, lit: true);

            // 02 low hall: the first vertical choice - hang and drop, or take the plate route
            Room(2, "低廳", 20f, 40f, -8f, 8.25f);
            Slab("Floor_LowHall_Upper_A", 20f, 26f, 0f, 1.2f, _sandstone);
            Slab("Ledge_LowHall_Mid", 29f, 33f, 0f, 1f, _sandstoneDark);
            Slab("Ledge_LowHall_Exit", 35.6f, 39f, 0f, 1f, _sandstoneDark);
            Slab("Floor_LowHall_Lower", 20f, 40f, -3.2f, 1.4f, _sandstone);
            // Too tall to hop, and tucked under the upper ledge so it only ever
            // blocks the lower corridor.
            var lowDoor = Door("Door_LowHall", 37.4f, 38.4f, 0f, 3.2f, new Vector2(0f, -3.4f));
            Plate("Plate_LowHall", 30f, -3.2f, true, lowDoor);
            Chain("Chain_LowHall", 39.6f, -3.2f, 0.8f);
            Milk("Milk_LowHall", 31f, 0f);
            Lantern(lanterns, 2, "低廳", 21.5f, 0f);

            // 03 gravel gallery: read a cycle, then read a cycle while the floor leaves
            Room(3, "砂礫廊", 40f, 60f, -6f, 8f);
            Slab("Floor_Gravel_A", 40f, 52f, 0f, 1.2f, _sandstone);
            Spikes("Spikes_Gravel", 46.2f, 49f, 0f, 0f);
            FixedSpikePit("Pit_Gravel", 52f, 58f, -3f, 1.2f);
            Brick("Brick_Gravel_A", 53f, 0.6f);
            Brick("Brick_Gravel_B", 55f, 0.6f);
            Brick("Brick_Gravel_C", 57f, 0.6f);
            Slab("Floor_Gravel_B", 58f, 60f, 0f, 1.2f, _sandstone);
            Lantern(lanterns, 3, "砂礫廊", 41f, 0f);

            // 04 broken bridge: high road past a saw, or the long way down and up a chain
            Room(4, "斷橋", 60f, 80f, -10f, 8f);
            Slab("Floor_Bridge_A", 60f, 64f, 0f, 1.2f, _sandstone);
            Slab("Step_Bridge", 66f, 68f, 1f, 0.8f, _sandstoneDark);
            Slab("Ledge_Bridge_High", 70f, 74f, 2f, 0.8f, _sandstoneDark);
            Saw("Saw_Bridge", new Vector2(66.5f, 2.9f), new Vector2(7.5f, 0f), 3.6f);
            Slab("Floor_Bridge_Low", 62f, 78f, -3f, 1.4f, _sandstone);
            Chain("Chain_Bridge", 76f, -3f, 2.6f);
            Slab("Ledge_Bridge_Exit", 77.5f, 80f, 2.2f, 1f, _sandstoneDark);
            Lantern(lanterns, 4, "斷橋", 61f, 0f);

            // 05 guard hall: the duel, and a plate either fighter can stand on
            Room(5, "守衛門廳", 78f, 100f, -4f, 10f);
            Slab("Floor_GuardHall", 78f, 100f, 2.2f, 1.4f, _sandstone);
            Pillar("Pillar_05a", 82f, 2.2f, 9f);
            Pillar("Pillar_05b", 95f, 2.2f, 9f);
            var guardDoor = Door("Door_GuardHall", 98.6f, 99.6f, 6.2f, 4f, new Vector2(0f, 3.8f));
            // The guard is heavy enough to work the plate itself, so luring it
            // onto the gate is a real second answer to this screen.
            Plate("Plate_GuardHall", 95.5f, 2.2f, true, guardDoor);
            var jackal = Enemy<SandJackalGuard>("SandJackalGuard", "Assets/Art/Enemies/Sliced/enemy_00.png",
                new Vector3(91f, 2.2f, 0f), new Vector2(1.1f, 2.3f));
            jackal.solidMask = solidMask;
            jackal.leftBound = Marker("JackalLeft", new Vector3(85f, 2.2f, 0f));
            jackal.rightBound = Marker("JackalRight", new Vector3(93f, 2.2f, 0f));
            Lantern(lanterns, 5, "守衛門廳", 79.5f, 2.2f);

            // 06 twin galleries: a timed lever upstairs, a latching plate downstairs
            Room(6, "雙迴廊", 100f, 120f, -2f, 12f);
            Slab("Floor_Twin_Lower", 100f, 120f, 2.2f, 1.4f, _sandstone);
            Slab("Step_Twin_A", 101f, 103f, 3.6f, 0.8f, _sandstoneDark);
            Slab("Ledge_Twin_Upper", 105f, 111f, 5.4f, 0.9f, _sandstoneDark);
            var twinDoor = Door("Door_Twin", 115.6f, 116.6f, 6.2f, 4f, new Vector2(0f, 3.8f));
            var twinSpikes = Spikes("Spikes_Twin", 117.4f, 119.4f, 2.2f, 0.7f);
            Lever("Lever_Twin", 110f, 5.4f, 16f, twinDoor);
            Plate("Plate_Twin", 104f, 2.2f, true, twinSpikes);
            var wraith = Enemy<CarpetWraith>("CarpetWraith", "Assets/Art/Enemies/Sliced/enemy_01.png",
                new Vector3(109f, 8.2f, 0f), new Vector2(2.6f, 1.4f));
            wraith.laneHalfWidth = 5f;
            Milk("Milk_Twin", 107.5f, 5.4f);
            Lantern(lanterns, 6, "雙迴廊", 100.8f, 2.2f);

            // 07 collapsing shaft: a staircase that will not wait, with the chain as the safety line
            Room(7, "塌柱井", 118f, 140f, -16f, 8.25f);
            Slab("Floor_Shaft_Top", 118f, 124f, 2.2f, 1.4f, _sandstone);
            Brick("Brick_Shaft_A", 125.4f, 0.4f);
            Brick("Brick_Shaft_B", 128.4f, -1.6f);
            Brick("Brick_Shaft_C", 125.4f, -3.6f);
            Brick("Brick_Shaft_D", 128.4f, -5.6f);
            Brick("Brick_Shaft_E", 125.6f, -7.6f);
            Chain("Chain_Shaft", 131.5f, -9.6f, 2.4f);
            Slab("Floor_Shaft_Bottom", 118f, 140f, -10f, 1.6f, _sandstone);
            Pillar("Pillar_07", 136f, -10f, 2f);
            Lantern(lanterns, 7, "塌柱井", 121f, -10f);

            // 08 spear vault: no jumping past this one, only footing
            Room(8, "槍兵庫", 140f, 160f, -16f, -2f);
            Slab("Floor_Vault", 140f, 160f, -10f, 1.6f, _sandstone);
            Slab("Step_Vault_A", 143f, 145f, -8.4f, 0.8f, _sandstoneDark);
            Slab("Ledge_Vault_Post", 147f, 152f, -7f, 0.9f, _sandstoneDark);
            Slab("Ledge_Vault_Exit", 154f, 160f, -6f, 0.9f, _sandstoneDark);
            var spearman = Enemy<ScarabSpearman>("ScarabSpearman", "Assets/Art/Enemies/Sliced/enemy_02.png",
                new Vector3(150.5f, -7f, 0f), new Vector2(1.2f, 2.4f));
            spearman.solidMask = solidMask;
            spearman.postHalfWidth = 2.2f;
            Lantern(lanterns, 8, "槍兵庫", 141f, -10f);

            // 09 reversal hall: pull it at the far end, then come back for the door you opened
            Room(9, "反轉廳", 160f, 180f, -14f, 0f);
            Slab("Floor_Reversal", 160f, 180f, -6f, 1.6f, _sandstone);
            Slab("Step_Reversal", 162f, 164f, -4.4f, 0.8f, _sandstoneDark);
            Slab("Ledge_Reversal", 164f, 180f, -2.6f, 0.9f, _sandstoneDark);
            var reversalDoor = Door("Door_Reversal", 164f, 165.2f, 0.4f, 5.4f, new Vector2(0f, -5.6f));
            Lever("Lever_Reversal", 176.5f, -6f, 16f, reversalDoor);
            var wraith2 = Enemy<CarpetWraith>("CarpetWraith_Reversal", "Assets/Art/Enemies/Sliced/enemy_01.png",
                new Vector3(171f, -4.2f, 0f), new Vector2(2.6f, 1.4f));
            wraith2.laneHalfWidth = 4.5f;
            Lantern(lanterns, 9, "反轉廳", 161f, -6f);

            // 10 hourglass forecourt: everything learned so far, at once, with the sand low
            Room(10, "沙漏前庭", 180f, 200f, -10f, 2f);
            Slab("Floor_Forecourt_A", 180f, 191f, -2.6f, 1.4f, _sandstone);
            Spikes("Spikes_Forecourt_A", 185.6f, 188f, -2.6f, 0f);
            Spikes("Spikes_Forecourt_B", 189f, 190.6f, -2.6f, 1.1f);
            FixedSpikePit("Pit_Forecourt", 191f, 198f, -6f, 1.2f);
            Brick("Brick_Forecourt_A", 192.6f, -2f);
            Brick("Brick_Forecourt_B", 194.8f, -2f);
            Brick("Brick_Forecourt_C", 197f, -2f);
            Slab("Floor_Forecourt_B", 198f, 202f, -2.6f, 1.4f, _sandstone);
            Lantern(lanterns, 10, "沙漏前庭", 181f, -2.6f);

            // 11 djinn well: three readable phases and a core that is only open on its own terms
            Room(11, "精靈井", 200f, 224f, -9f, 5f);
            Slab("Floor_Well", 200f, 224f, -2.6f, 1.6f, _sandstone);
            Chain("Chain_Well_A", 205f, -2.6f, 4.4f);
            Chain("Chain_Well_B", 219f, -2.6f, 4.4f);
            var snuffA = Door("Platform_Well_A", 207.5f, 211.5f, -0.2f, 0.8f, new Vector2(0f, -14f), false, false, _sandstoneDark);
            var snuffB = Door("Platform_Well_B", 213f, 217f, -0.2f, 0.8f, new Vector2(0f, -14f), false, false, _sandstoneDark);
            var wellExit = Door("Door_Well_Exit", 222.6f, 223.8f, 2.4f, 5.2f, new Vector2(0f, 5f), false, false, _moonstone);

            var keyGo = Prop("MoonKey", _moonKeySprite, new Vector3(212f, -2.2f, 0f), 8);
            TriggerBox(keyGo, new Vector2(1.2f, 1.2f), new Vector2(0f, 0.5f));
            keyGo.AddComponent<MoonKeyItem>();
            keyGo.SetActive(false);

            var djinn = Enemy<HourglassDjinn>("HourglassDjinn", "Assets/Art/Enemies/Sliced/enemy_03.png",
                new Vector3(212f, -2.6f, 0f), new Vector2(2f, 3f));
            djinn.bladePrefab = BuildSandBladePrefab(solidMask);
            djinn.columnPrefab = BuildSandColumnPrefab();
            djinn.reward = keyGo;
            djinn.shutdownTargets = new List<MonoBehaviour> { wellExit };
            djinn.snuffTargets = new List<MonoBehaviour> { snuffA, snuffB };
            djinn.arenaHalfWidth = 8f;
            Lantern(lanterns, 11, "精靈井", 201.2f, -2.6f);

            // 12 moon gate: a quiet walk, then three conditions and the slice ends
            Room(12, "月門", 224f, 246f, -9f, 5f);
            Slab("Floor_MoonGate", 224f, 246f, -2.6f, 1.6f, _sandstone);
            Pillar("Pillar_12a", 230f, -2.6f, 3.5f);
            Pillar("Pillar_12b", 234f, -2.6f, 3.5f);
            var gateSlab = Slab("MoonGate_Door", 239.5f, 241.5f, 3.4f, 6f, _moonstone, 2);
            var gate = gateSlab.AddComponent<MoonGate>();
            gate.gateDoor = gateSlab.transform;
            gate.openOffset = new Vector3(0f, 6.4f, 0f);
            var keyhole = Prop("MoonGate_Keyhole", _moonKeySprite, new Vector3(238.4f, -1.4f, 0f), 6);
            gate.keyhole = keyhole.GetComponent<SpriteRenderer>();
            var gateTrigger = new GameObject("MoonGate_Slot");
            gateTrigger.transform.SetParent(gateSlab.transform, false);
            gateTrigger.transform.position = new Vector3(238.4f, -1.4f, 0f);
            TriggerBox(gateTrigger, new Vector2(2.6f, 3f), new Vector2(0f, 0.6f));
            Lantern(lanterns, 12, "月門", 225.5f, -2.6f);
        }

        static SandBlade BuildSandBladePrefab(int solidMask)
        {
            var go = new GameObject("SandBlade");
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3(-500f, -500f, 0f);   // parked off level; instantiated at runtime
            var sr = go.AddComponent<SpriteRenderer>();
            sr.sprite = _sandBladeSprite;
            sr.sortingOrder = 9;
            var blade = go.AddComponent<SandBlade>();
            blade.solidMask = solidMask;
            go.SetActive(false);
            return blade;
        }

        static SandColumn BuildSandColumnPrefab()
        {
            var go = new GameObject("SandColumn");
            go.transform.SetParent(_world, false);
            go.transform.position = new Vector3(-500f, -500f, 0f);
            var art = new GameObject("Sand");
            art.transform.SetParent(go.transform, false);
            var sr = art.AddComponent<SpriteRenderer>();
            sr.sprite = _sandstone;
            sr.drawMode = SpriteDrawMode.Sliced;
            sr.size = new Vector2(1.2f, 0.2f);
            sr.sortingOrder = 9;
            sr.color = new Color(0.95f, 0.82f, 0.55f, 0.9f);
            var column = go.AddComponent<SandColumn>();
            go.SetActive(false);
            return column;
        }

        // ---- hud -------------------------------------------------------------------

        static void BuildHud(GameDirector director)
        {
            var canvasGo = new GameObject("HUD", typeof(Canvas), typeof(CanvasScaler), typeof(GraphicRaycaster));
            var canvas = canvasGo.GetComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            var scaler = canvasGo.GetComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(ArtPipeline.RefX, ArtPipeline.RefY);
            scaler.matchWidthOrHeight = 1f;

            var font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            var hud = canvasGo.AddComponent<HudView>();

            var pawFull = ArtPipeline.Load("Assets/Art/UI/paw_full.png");
            var pawHurt = ArtPipeline.Load("Assets/Art/UI/paw_hurt.png");
            var pawEmpty = ArtPipeline.Load("Assets/Art/UI/paw_empty.png");
            var handIcon = ArtPipeline.Load("Assets/Art/UI/icon_hand.png");
            var panel = ArtPipeline.Load("Assets/Art/UI/panel.png");
            var hourglassIcon = ArtPipeline.LoadAny("Assets/Art/Props/Loot/loot_01.png", "Assets/Art/UI/paw_full.png");

            Text MakeText(string name, Vector2 anchor, Vector2 pos, TextAnchor align, int size, Color color)
            {
                var go = new GameObject(name);
                go.transform.SetParent(canvasGo.transform, false);
                var text = go.AddComponent<Text>();
                text.font = font;
                text.fontSize = size;
                text.alignment = align;
                text.color = color;
                text.horizontalOverflow = HorizontalWrapMode.Overflow;
                text.verticalOverflow = VerticalWrapMode.Overflow;
                var rt = text.rectTransform;
                rt.anchorMin = rt.anchorMax = rt.pivot = anchor;
                rt.anchoredPosition = pos;
                rt.sizeDelta = new Vector2(460f, 40f);
                return text;
            }

            Image MakeImage(string name, Sprite sprite, Vector2 anchor, Vector2 pos, Vector2 size, Transform parent = null)
            {
                var go = new GameObject(name);
                go.transform.SetParent(parent != null ? parent : canvasGo.transform, false);
                var image = go.AddComponent<Image>();
                image.sprite = sprite;
                image.raycastTarget = false;
                var rt = image.rectTransform;
                rt.anchorMin = rt.anchorMax = rt.pivot = anchor;
                rt.anchoredPosition = pos;
                rt.sizeDelta = size;
                return image;
            }

            // three paws, top left
            var paws = new Image[3];
            for (int i = 0; i < 3; i++)
                paws[i] = MakeImage($"Paw_{i}", pawFull, new Vector2(0f, 1f), new Vector2(14f + i * 26f, -14f), new Vector2(22f, 22f));
            hud.paws = paws;
            hud.pawFull = pawFull;
            hud.pawHurt = pawHurt;
            hud.pawEmpty = pawEmpty;

            // royal hourglass, top right: icon, a sand column that drains, and the clock
            MakeImage("HourglassIcon", hourglassIcon, new Vector2(1f, 1f), new Vector2(-14f, -10f), new Vector2(22f, 34f));
            var sandTrack = MakeImage("SandTrack", panel, new Vector2(1f, 1f), new Vector2(-42f, -14f), new Vector2(84f, 8f));
            sandTrack.color = new Color(0.12f, 0.1f, 0.14f, 0.85f);
            var sandFill = MakeImage("SandFill", panel, new Vector2(0f, 0.5f), new Vector2(0f, 0f), new Vector2(84f, 8f), sandTrack.transform);
            sandFill.color = new Color(0.95f, 0.82f, 0.5f, 1f);
            sandFill.type = Image.Type.Filled;
            sandFill.fillMethod = Image.FillMethod.Horizontal;
            sandFill.fillOrigin = (int)Image.OriginHorizontal.Right;
            hud.sandFill = sandFill;
            hud.timeText = MakeText("HourglassTime", new Vector2(1f, 1f), new Vector2(-42f, -26f), TextAnchor.UpperRight, 14,
                new Color(0.94f, 0.92f, 0.86f));

            var frame = MakeImage("LowSandFrame", panel, new Vector2(0.5f, 0.5f), Vector2.zero, Vector2.zero);
            var frameRt = frame.rectTransform;
            frameRt.anchorMin = Vector2.zero;
            frameRt.anchorMax = Vector2.one;
            frameRt.sizeDelta = Vector2.zero;
            frame.color = new Color(1f, 0.5f, 0.28f, 0f);
            hud.lowSandFrame = frame;

            // prompt: one glyph and one short line, only inside reach
            var promptRoot = new GameObject("Prompt");
            promptRoot.transform.SetParent(canvasGo.transform, false);
            var promptRt = promptRoot.AddComponent<RectTransform>();
            promptRt.anchorMin = promptRt.anchorMax = promptRt.pivot = new Vector2(0.5f, 0f);
            promptRt.anchoredPosition = new Vector2(0f, 54f);
            promptRt.sizeDelta = new Vector2(300f, 30f);
            var promptIcon = MakeImage("PromptIcon", pawFull, new Vector2(0.5f, 0.5f), new Vector2(-52f, 0f), new Vector2(20f, 20f), promptRoot.transform);
            var promptLabelGo = new GameObject("PromptLabel");
            promptLabelGo.transform.SetParent(promptRoot.transform, false);
            var promptLabel = promptLabelGo.AddComponent<Text>();
            promptLabel.font = font;
            promptLabel.fontSize = 15;
            promptLabel.alignment = TextAnchor.MiddleLeft;
            promptLabel.color = new Color(0.96f, 0.93f, 0.84f);
            promptLabel.horizontalOverflow = HorizontalWrapMode.Overflow;
            var plRt = promptLabel.rectTransform;
            plRt.anchorMin = plRt.anchorMax = plRt.pivot = new Vector2(0.5f, 0.5f);
            plRt.anchoredPosition = new Vector2(30f, 0f);
            plRt.sizeDelta = new Vector2(220f, 24f);

            hud.promptRoot = promptRoot;
            hud.promptIcon = promptIcon;
            hud.promptLabel = promptLabel;
            hud.iconPaw = pawFull;
            hud.iconHand = handIcon;
            hud.iconBlade = ArtPipeline.LoadAny("Assets/Art/Props/Loot/loot_00.png");
            hud.iconMoon = ArtPipeline.LoadAny("Assets/Art/Props/Loot/loot_02.png");

            hud.bannerText = MakeText("Banner", new Vector2(0.5f, 0.5f), new Vector2(0f, 0f), TextAnchor.MiddleCenter, 22,
                new Color(0.98f, 0.93f, 0.8f));
            hud.screenText = MakeText("ScreenName", new Vector2(0.5f, 1f), new Vector2(0f, -18f), TextAnchor.UpperCenter, 15,
                new Color(0.85f, 0.8f, 0.72f, 0f));
            hud.hintText = MakeText("Hint", new Vector2(0.5f, 0f), new Vector2(0f, 16f), TextAnchor.LowerCenter, 13,
                new Color(0.7f, 0.67f, 0.62f));
        }
    }
}
