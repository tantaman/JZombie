var express = require('express');
var app = express.createServer();
var state = require('./state');
var faye = require('faye');
var _ = require('underscore');

app.use(express.bodyParser());

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

app.put('/ItemList/:id', function(req, res) {
	console.log(req.body);

	var models = state.ItemList.models;

	var existingModel = null;
	models.every(function(model) {
		if (model.id == req.body.id) {
			existingModel = model;
			return false;
		}

		return true;
	});

	if (existingModel == null) {
		models.unshift(req.body);
		bayClient.publish('/ItemList/' + req.params.id, existingModel);
	} else {
		_.extend(
		existingModel, 
		req.body);

		bayClient.publish('/ItemList/' + req.params.id, existingModel);
	}

	res.send();
});

var bayeux = new faye.NodeAdapter({mount: '/bayeux', timeout: 45});
bayeux.attach(app);

var bayClient = bayeux.getClient();

app.listen(80);

console.log("Up and listening at port 80");