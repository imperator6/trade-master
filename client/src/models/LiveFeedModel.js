import { observable, computed, action } from "mobx";

export default class LiveFeedModel {

   @observable livefeed2 = true;
   @observable livefeedLabel = "Enabel Livefeed";

   @action.bound
   changeLiveUpdate() {
    console.log(this.livefeed2);
       if(this.livefeed2) {
        this.livefeed2 = 0;
       } else {
           this.livefeed2 = 1;
       }
   }

   @action.bound
   changeLiveFeedLabel() {
       console.log(this.livefeed2);
       if(this.livefeed2) {
        this.livefeedLabel = "Disable Livefeed";
       } else {
        this.livefeedLabel = "Enable Livefeed";
       }
   }

}