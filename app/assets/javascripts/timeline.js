;(function() {

  var input = document.querySelector('input'),
      tweets = document.querySelector('.tweets'),
      results = document.querySelector('.results'),
      info = results.querySelector('.info'),
      clearBtn = info.querySelector('.clear'),
      stopBtn = info.querySelector('.stop'),
      keyword = info.querySelector('.keyword'),
      source;

  var timeline = {
    start: function (kw) {
      source && source.close();
      source = new EventSource(Routes.controllers.Application.tweets(kw).url);
      source.onmessage = function (e) {
        timeline.ui.put(e.data);
      };
      source.onerror = function (e) {
        console.log("error", e)
      };

      timeline.ui.start(kw);
    },
    stop: function () {
      source.close();
      timeline.ui.stop();
    },

    ui: {
      start: function (kw) {
        keyword.textContent = kw;
        info.style.display = 'block';
        input.value = '';
      },
      put: function (htmlContent) {
        var tweet = document.createElement('li');
        tweet.innerHTML = htmlContent;
        tweets.insertBefore(tweet, tweets.firstChild);
      },
      clear: function () {
        tweets.innerHTML = '';
      },
      stop: function () {
        info.style.display = 'none';
      }
    }
  };

  input.addEventListener('keydown', function (e) {
    if (e.keyCode == 13) { // Enter has been pressed
      timeline.start(input.value);
    }
  });

  clearBtn.addEventListener('click', function (e) {
    timeline.ui.clear();
  });

  stopBtn.addEventListener('click', function (e) {
    timeline.stop();
  });

}());
