# Sim4stamp

The simulation tool for STAMP/STPA

「`Sim4stamp`」はSTAMP/STPA手法を支援するシミュレーションツールです。

## 履歴

2020/05/04 Windows版について、インストーラを追加する。
           WindowsInstaller下のSim4stampSetup.exeをダウンロードして、実行することにより、Sim4stampのインストールができる。

2020/04/12 jlinkで生成したJava実行環境で `/styles/Styles.css`が読み込めない現象を回避する修正を実施。

2020/04/11 jdk9以降で導入されたモジュールシステム化を実施する。
           実行ファイルは、JavaVMを含めたものを生成し、OSにインストールされたJavaVMと関係なく動作するようにした。
           ただし、`/styles/Styles.css`が読み込めない現象が発生するため、「結果表」の逸脱しているセルの色替えが
           行われない。

2019/10/21 5値論理を用いる説明用の事例を追加。

2019/10/14 取説に5値論理等の扱いに関する説明を追加する。

2019/08/15 一連の偏差投入シミュレーションを一括で行えるようにし、結果についてもCSV形式で出力できるようにした

2019/06/02 VDM++側でシミュレーションの初期値設定ができるようにする

2019/05/12 5値論理機能の初期的な追加を行う

2018/11/05 取扱い説明書をUpdate

2018/09/30 結果グラフ等、いろいろ機能改善

2018/06/02 シミュレーション変数の種類の追加、ステップ実行機能の追加等、いろいろ機能改善

2018/03/14 シミュレーション計算時のエレメント計算順の改善

2018/03/11 VDM++の生成時のバグ修正

2018/03/11 偏差投入画面の表示内容（自動表示）を改善する。

2018/03/05 VDM++側との通信手順とTCP-IPコネクション方法を一部変更。

2018/02/10 偏差投入の設定画面を新たに追加し、ビジュアル化する。


## 使用方法

本リポジトリ下の「documents」以下を参照ください。

## 起動方法

   bin下にある「sim4stampzip-X.X.X.zip」をダウンロードし、任意のフォルダに解凍します。
   解凍したフォルダの「bin/sim4stamp.bat」をクリックすると画面が表示されます。
   なお、本処理については専用のJavaVMが内蔵されていますが、Overtureについては、Overtureで指定
   するJavaをインストールしておく必要があります。

   上記は、Windowsの場合です。Windows以外のOSの場合は、JavaFXのライブラリをダウンロードし、
   クラスパスに追加し、Javaを起動する必要があります。
```
  java --module-path JavaFXのライブラリパス --add-modules=javafx.controls,javafx.fxml -jar sim4stamp-1.0.jar
```

   例えば「ubuntu」では、「apt-get install openjfx」でJavaFXライブラリを導入すると「/usr/share/openjfx/lib」に
   ライブラリができるので、以下のように指定して起動します(jdk11環境下)。
```
  java --module-path /usr/share/openjfx/lib --add-modules=javafx.controls,javafx.fxml -jar sim4stamp-1.0.jar
```

## ライセンス

ソースコードのライセンスはGPL-v3です。

## Mavenによるビルド方法

jdk11以降の環境で以下の手順で行います。

（１）コンパイル/実行ファイル（jar）生成

```
   mvn clean package
```

（２）JavaVMイメージ生成

```
   mvn javafx:jlink

```
（３）実行

```
  mvn javafx:run
```
  または
```
  target\sim4stamp\bin\sim4stamp
```
  または
```
  java --module-path PARAM1 --add-modules=javafx.controls,javafx.fxml -jar sim4stamp-1.0.jar
```
  ここで、「PARAM1」はJavaFXのライブラリのパスを与える（ex C:\javafx-sdk-14\lib）。

