var app = require('express').createServer();
var state = require('./state');
var faye = require('faye');
var _ = require('underscore');


var nextItemId = 3;
app.get('/ItemList', function(req, res){
	res.send(state.ItemList)
});

app.get('/ItemList/:id', function(req, res) {
	var id = req.params.id;
	res.send(_.filter(state.ItemList.items, function(item) {
		if (item.id == id) {
			return item;
		}
	})[0]);
});

app.post('/ItemList', function(req, res) {

});

app.put('/ItemList/:id', function(req, res) {

});

var bayeux = new faye.NodeAdapter({mount: '/bayeux', timeout: 45});
bayeux.attach(app);

app.listen(80);