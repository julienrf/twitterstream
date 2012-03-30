;(function() {

  var input = document.querySelector('input'),
      source;

  input.addEventListener('keydown', function (e) {
    if (e.keyCode == 13) { // Enter has been pressed
      source && source.close();
      source = new EventSource(Routes.controllers.Application.tweets(input.value).url);
      source.onmessage = function (e) {
        console.log(e.data);
      };
      source.onerror = function (e) {
        console.log("error", e)
      };

      input.value = '';
    }
  });

}());
