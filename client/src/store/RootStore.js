import StrategyStore from "./StrategyStore"

export default class RootStore {
    
    constructor() {
        this.strategyStore = new StrategyStore(this)
        //this.todoStore = new TodoStore(this)
    }


    
}