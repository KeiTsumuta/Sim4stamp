# Sim4stamp

The simulation tool for STAMP/STPA

「Sim4stamp」はSTAMP/STPA手法を支援するシミュレーションツールです。

## 履歴

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

## jdk11以降による起動方法

jdk11以降は、javaFXライブラリが同胞されてないので、別途、javaFXライブラリ（使用OSに該当するSDKライブラリ）を以下からダウンロードし、ライブラリを指定して起動してください。

https://gluonhq.com/products/javafx/

java --module-path AAA/javafx-sdk-11/lib --add-modules=javafx.controls,javafx.fxml  -cp sim4stamp-1.0.jar tmu.fs.sim4stamp.MainApp

ここで「AAA」はjavaFXのライブラリのルートパスを示します。実行環境に合わせて設定してください。


## ライセンス

ソースコードのライセンスはGPL-v3です。

