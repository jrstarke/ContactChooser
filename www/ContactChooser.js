/**
 * @constructor
 */
var ContactChooser = function(){};

ContactChooser.prototype.fieldType = {
	email: 1,
	phone: 2
};

ContactChooser.prototype.chooseContact = function(success, failure, fieldType){
	if (fieldType) {
		cordova.exec(success, failure, "ContactChooser", "chooseContact", [fieldType]);
	}
	else {
		cordova.exec(success, failure, "ContactChooser", "chooseContact", [this.fieldType.email]);	
	}
};

// Plug in to Cordova
cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };


    if(!window.plugins) window.plugins = {};
    window.plugins.ContactChooser = new ContactChooser();
});
