
var count = 0;


var fun1 = function(name) {
    print('Hi there from Javascript, ' + name);
    this.count++;
    return "greetings from javascript" + this.market.name;
};

var fun2 = function (object) {
    print("JS Class Definition: " + Object.prototype.toString.call(object));
};