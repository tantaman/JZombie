var app = require('express').createServer();
var state = require('./state');
var faye = require('faye');

app.get('/ItemList', function(req, res){
  
});

app.get('/ItemList/:id', function(req, res) {

});

var bayeux = new faye.NodeAdapter({mount: '/bayeux', timeout: 45});
bayeux.attach(app);

app.listen(80);