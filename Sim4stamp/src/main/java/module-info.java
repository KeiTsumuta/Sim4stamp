/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

module sim4stamp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.desktop;
    
    opens tmu.fs.sim4stamp to javafx.fxml;
    opens tmu.fs.sim4stamp.gui to javafx.fxml, javafx.base;
    exports tmu.fs.sim4stamp;
    exports tmu.fs.sim4stamp.tcp;
}
