import React from "react";
import { render } from "react-dom";
import DevTools from "mobx-react-devtools";

import TodoList from "./components/TodoList";
import TodoListModel from "./models/TodoListModel";
import TodoModel from "./models/TodoModel";
import PriceDisplay from './components/PriceDisplay';

const store = new TodoListModel();

const products = ['Germany,Base,Power,All', 'Germany,Peak,Power,All', 'France,Base,Power,All']

const periodGroups2 = [{ groupName: 'Month', periods: ['Nov-17','Dec-17','Jan-18']},
{ groupName: 'Year', periods: ['Cal17','Cal18','Cal19']}];


render(
  <div>
    <DevTools />
    <TodoList store={store} />
    <PriceDisplay  products={products}  periodGroups={periodGroups2} ></PriceDisplay>
  </div>,
  document.getElementById("root")
  )

store.addTodo("Get Coffee");
store.addTodo("Write simpler code");
store.todos[0].finished = true;

setTimeout(() => {
  store.addTodo("Get a cookie as well");
}, 2000);

// playing around in the console
window.store = store;
