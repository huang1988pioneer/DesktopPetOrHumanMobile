using System.Collections.Generic;
using System.Linq;
using UnityEditor;
using UnityEngine;

namespace HourglassPalace.EditorTools
{
    /// <summary>
    /// The art arrives at wildly different pixel sizes, so the import step is
    /// where scale is decided: every sprite is given the pixels-per-unit that
    /// makes it the world height the design calls for, with point sampling, no
    /// compression and a bottom-centre pivot so things stand on the floor.
    /// </summary>
    public static class ArtPipeline
    {
        public const int TilePpu = 32;          // a 16px tile is half a world unit
        public const int RefX = 640;
        public const int RefY = 360;

        /// <summary>Target world height in units, by asset path fragment.</summary>
        static readonly (string Fragment, float Height)[] Heights =
        {
            ("/Art/Hero/miaobaibai_idle_strip", 2.44f),
            ("/Art/Hero/miaobaibai_walk_strip", 2.44f),
            ("/Art/Hero/miaobaibai.png", 2.44f),
            ("/Art/Enemies/Sliced/enemy_00", 2.5f),   // sand jackal guard
            ("/Art/Enemies/Sliced/enemy_01", 2.2f),   // carpet wraith
            ("/Art/Enemies/Sliced/enemy_02", 2.6f),   // scarab spearman
            ("/Art/Enemies/Sliced/enemy_03", 3.2f),   // hourglass djinn
            ("/Art/Props/Sliced/trap_00", 1.0f),      // spike bed
            ("/Art/Props/Sliced/trap_01", 1.35f),     // wall lever
            ("/Art/Props/Sliced/trap_02", 0.7f),      // milk bowl
            ("/Art/Props/Sliced/trap_03", 0.55f),     // pressure plate
            ("/Art/Props/Sliced/trap_04", 2.0f),      // long chain
            ("/Art/Props/Sliced/trap_05", 1.5f),      // saw blade
            ("/Art/Props/Sliced/trap_06", 1.35f),     // lantern
            ("/Art/Props/Sliced/trap_07", 1.0f),      // crumbling brick
            ("/Art/Props/Sliced/trap_08", 1.4f),      // short chain
            ("/Art/Props/Loot/loot_02", 1.0f),        // moon key
        };

        public static float HeightFor(string path)
        {
            path = path.Replace('\\', '/');
            foreach (var (fragment, height) in Heights)
                if (path.Contains(fragment)) return height;
            return 0f;
        }

        [MenuItem("Hourglass Palace/Reimport Art")]
        public static void ReimportAll()
        {
            var guids = AssetDatabase.FindAssets("t:Texture2D", new[] { "Assets/Art" });
            try
            {
                AssetDatabase.StartAssetEditing();
                foreach (var guid in guids)
                    Apply(AssetDatabase.GUIDToAssetPath(guid));
            }
            finally
            {
                AssetDatabase.StopAssetEditing();
                AssetDatabase.Refresh();
            }
            Debug.Log($"[ArtPipeline] {guids.Length} textures reimported at pixel-art settings.");
        }

        static void Apply(string path)
        {
            var importer = AssetImporter.GetAtPath(path) as TextureImporter;
            if (importer == null) return;

            var texture = AssetDatabase.LoadAssetAtPath<Texture2D>(path);
            int pixelHeight = texture != null ? texture.height : 0;
            string normalised = path.Replace('\\', '/');
            bool isStrip = normalised.Contains("_strip");
            bool isMapLayer = normalised.Contains("/Art/Map/");
            bool isUi = normalised.Contains("/Art/UI/");
            bool isTile = normalised.Contains("/Art/Tiles/");

            importer.textureType = TextureImporterType.Sprite;
            importer.filterMode = FilterMode.Point;
            importer.mipmapEnabled = false;
            importer.textureCompression = TextureImporterCompression.Uncompressed;
            importer.alphaIsTransparency = true;
            importer.npotScale = TextureImporterNPOTScale.None;
            importer.wrapMode = TextureWrapMode.Clamp;
            importer.maxTextureSize = 4096;

            float ppu = TilePpu;
            if (isStrip)
            {
                // 8 frames across one strip; pivot at the feet so every state lines up
                int cells = Mathf.Max(1, texture != null ? texture.width / 64 : 8);
                importer.spriteImportMode = SpriteImportMode.Multiple;
                importer.spritesheet = BuildStripSheet(path, texture, cells);
                ppu = pixelHeight / Mathf.Max(0.01f, HeightFor(path));
            }
            else
            {
                importer.spriteImportMode = SpriteImportMode.Single;
                float wanted = HeightFor(path);
                if (wanted > 0f && pixelHeight > 0) ppu = pixelHeight / wanted;
                else if (isMapLayer && pixelHeight > 0) ppu = pixelHeight / 16f;   // one backdrop ~1.4 screens
                else if (isUi) ppu = 16f;
            }

            var settings = new TextureImporterSettings();
            importer.ReadTextureSettings(settings);
            settings.spriteMode = (int)importer.spriteImportMode;
            settings.spritePixelsPerUnit = ppu;
            settings.spriteMeshType = SpriteMeshType.FullRect;
            settings.spriteExtrude = isTile ? 1u : 0u;
            settings.alphaIsTransparency = true;
            settings.spriteAlignment = (int)(isMapLayer || isUi
                ? SpriteAlignment.Center
                : SpriteAlignment.BottomCenter);
            settings.spritePivot = isMapLayer || isUi ? new Vector2(0.5f, 0.5f) : new Vector2(0.5f, 0f);
            importer.SetTextureSettings(settings);
            importer.SaveAndReimport();
        }

        static SpriteMetaData[] BuildStripSheet(string path, Texture2D texture, int cells)
        {
            if (texture == null) return System.Array.Empty<SpriteMetaData>();
            int cellW = texture.width / cells;
            int cellH = texture.height;
            string stem = System.IO.Path.GetFileNameWithoutExtension(path);
            var sheet = new SpriteMetaData[cells];
            for (int i = 0; i < cells; i++)
            {
                sheet[i] = new SpriteMetaData
                {
                    name = $"{stem}_{i:00}",
                    rect = new Rect(i * cellW, 0, cellW, cellH),
                    alignment = (int)SpriteAlignment.BottomCenter,
                    pivot = new Vector2(0.5f, 0f)
                };
            }
            return sheet;
        }

        public static Sprite Load(string path) => AssetDatabase.LoadAssetAtPath<Sprite>(path);

        public static Sprite LoadAny(params string[] paths)
        {
            foreach (var path in paths)
            {
                var sprite = Load(path);
                if (sprite != null) return sprite;
            }
            return null;
        }

        /// <summary>All frames of a sliced strip, in strip order.</summary>
        public static Sprite[] LoadFrames(string path)
        {
            var all = AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToList();
            all.Sort((a, b) => string.CompareOrdinal(a.name, b.name));
            return all.ToArray();
        }

        /// <summary>Adds a layer to the project if it is not there yet, and returns its index.</summary>
        public static int EnsureLayer(string layerName)
        {
            int existing = LayerMask.NameToLayer(layerName);
            if (existing >= 0) return existing;

            var asset = AssetDatabase.LoadAllAssetsAtPath("ProjectSettings/TagManager.asset");
            if (asset == null || asset.Length == 0) return 0;
            var tagManager = new SerializedObject(asset[0]);
            var layers = tagManager.FindProperty("layers");
            for (int i = 8; i < layers.arraySize; i++)
            {
                var element = layers.GetArrayElementAtIndex(i);
                if (!string.IsNullOrEmpty(element.stringValue)) continue;
                element.stringValue = layerName;
                tagManager.ApplyModifiedPropertiesWithoutUndo();
                AssetDatabase.SaveAssets();
                return i;
            }
            Debug.LogWarning($"[ArtPipeline] No free layer slot for '{layerName}'.");
            return 0;
        }

        public static IEnumerable<string> AllArtPaths() =>
            AssetDatabase.FindAssets("t:Texture2D", new[] { "Assets/Art" })
                .Select(AssetDatabase.GUIDToAssetPath);
    }
}
