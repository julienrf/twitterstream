(function() {

  var source = new EventSource("/tweets/javascript")
  source.onmessage = function(event) {
    console.log(event.data)
  }
  source.onerror = function(event) {
    console.log("EventSource fired an error")
  }

}())
