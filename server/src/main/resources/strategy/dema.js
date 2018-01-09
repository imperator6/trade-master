var count = 0;

var EMA = function(weight) {
  this.weight = weight;
  this.result = false;
  this.age = 0;
};

EMA.prototype.update = function(price) {
  // The first time we can't calculate based on previous
  // ema, because we haven't calculated any yet.
  if(this.result === false)
    this.result = price;

  this.age++;
  this.calculate(price);

  return this.result;
};

//    calculation (based on tick/day):
//  EMA = Price(t) * k + EMA(y) * (1 – k)
//  t = today, y = yesterday, N = number of days in EMA, k = 2 / (N+1)
EMA.prototype.calculate = function(price) {
  // weight factor2
  var k = 2 / (this.weight + 1);

  // yesterday
  var y = this.result;

  // calculation
  this.result = price * k + y * (1 - k);
};

var shortEma = new EMA(10);
var longEma = new EMA(21);

var currenTrend = 'none';


var nextCandle = function(candle, params, actions) {
    this.count++;

    this.shortEma.update(candle.close);
    this.longEma.update(candle.close);

    var shortEMA = this.shortEma.result;
    var longEMA = this.longEma.result;

    var diff = 100 * (shortEMA - longEMA) / ((shortEMA + longEMA) / 2);

     if(diff > 0.025) {

         if(this.currentTrend !== 'up') {
            this.currentTrend = 'up';
            actions.buy();
         }

     } else if(diff < -0.025) {

         if(this.currentTrend !== 'down') {
            this.currentTrend = 'down';
            actions.sell();
        }
     }



    return null;
};