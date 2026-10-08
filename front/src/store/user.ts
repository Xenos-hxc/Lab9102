import { defineStore } from 'pinia'
export const userStore=defineStore('user',{
  state:()=>({
    username:''
  }),
  getters:{

    getUsername(state){
      return state.username
    }
  },
  actions:{
    changeName(){
      this.username='hx'
    }
  }
})
