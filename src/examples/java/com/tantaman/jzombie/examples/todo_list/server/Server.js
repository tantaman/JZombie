var express = require('express');
var app = express.createServer();
var state = require('./state');
var faye = require('faye');
var _ = require('underscore');

app.use(express.bodyParser());

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
	console.log(req.body);

	state.ItemList.models.unshift(req.body);

	res.send({id: nextItemId++});
});

app.put('/ItemList/:id', function(req, res) {
	console.log(req.body);

	_.extend(
		state.ItemList.models[state.ItemList.models.length - req.body.id - 1], 
		req.body);

	res.send();
});

var bayeux = new faye.NodeAdapter({mount: '/bayeux', timeout: 45});
bayeux.attach(app);

app.listen(80);

console.log("Up and listening at port 80");