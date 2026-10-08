import { ref } from 'vue'
export default  function (){
  const name=ref("hxc")
  function changeName(){
    name.value="hxc2"
  }
  return {
    name,
    changeName
  }
}
