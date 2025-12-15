# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリで作業する際のガイダンスを提供します。

## プロジェクト概要

金属加工・建築現場向けの三角形計算Androidアプリ。三角関数（正弦定理、余弦定理、三平方の定理）を使用して、三角形の未知の辺や角度を計算する。

- パッケージ: `com.anglecalc.app`
- 最小SDK: 24 (Android 7.0)
- UI: Jetpack Compose + Material 3

## ビルドコマンド

```bash
# デバッグAPKのビルド
./gradlew assembleDebug

# リリースAPKのビルド
./gradlew assembleRelease

# 全ユニットテスト実行
./gradlew test

# 単一テストクラスの実行
./gradlew test --tests "com.anglecalc.app.TriangleSolverTest"

# インストルメンテーションテスト実行（エミュレータ/実機が必要）
./gradlew connectedAndroidTest

# クリーンビルド
./gradlew clean
```

## アーキテクチャ

```
app/src/main/java/com/anglecalc/app/
├── MainActivity.kt      # 全画面のUI（ホーム、計算機、設定）- シングルアクティビティCompose
├── TriangleSolver.kt    # 三角形計算ロジック（反復アルゴリズム）
├── MemoryManager.kt     # 計算履歴の保存・読込（SharedPreferences + Gson）
├── SettingsManager.kt   # 小数点桁数設定（SharedPreferences）
└── ui/theme/            # Material 3テーマ（Color, Theme, Type）
```

### 主要コンポーネント

**TriangleSolver**: `solve(Triangle): Triangle` メソッドを持つシングルトン。最大5回の反復で以下を組み合わせて計算:
- 角度の合計則 (A + B + C = 180°)
- 正弦定理
- 余弦定理

入力は辺を1つ以上含む3つ以上の値が必要。無効な入力時は `SolverException` をスロー。

**データ永続化**: MemoryManagerとSettingsManagerはSharedPreferencesとGsonを使用してJSON形式で保存。
