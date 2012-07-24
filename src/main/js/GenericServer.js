var express = require('express');
var app = express.createServer();
var faye = require('faye');
var _ = require('underscore');
var mapResolver = require('./MapResolver');
var util = require('util');

app.use(express.bodyParser());

var bayeux = new faye.NodeAdapter({mount: '/bayeux', timeout: 45});
bayeux.attach(app);

app.use(function(req, res) {
	console.log(req.url);
	console.log(req.method);
	console.log(req.body);
	console.log(req.query);

	switch (req.method) {
		case 'GET':
			get(req, res);
		break;
		case 'POST':
			post(req, res);
		break;
		case 'PUT':
			put(req, res);
		break;
		case 'DELETE':
			del(req, res);
		break;
	}
});

var bayClient = bayeux.getClient();

app.listen(80);

var state = {};
var nextId = 0;

function get(req, res) {
	var parts = req.url.split("/");
	parts.shift();
	var data = mapResolver.resolveItem(state, parts);

	if (data != null)
		res.send(data);
	else {
		parts.pop();
		data = mapResolver.resolveItem(state, parts);
		if (data != null)
			res.send(data);
		else
			res.send();
	}
}

/**
TODO: make better use of ETags for all situation!
**/

function post(req, res) {
	var parts = req.url.split("/");
	parts.shift();
	var data = mapResolver.resolveItem(state, parts);

	req.body.id = nextId++;

	if (data == null || !Array.isArray(data)) {
		data = [];
		mapResolver.placeItem(state, parts, data);
	}

	data.push(req.body);

	res.send();
}

function put(req, res) {
	var parts = req.url.split("/");
	var etag = req.query.etag;
	parts.shift();

	if (req.body.id != null) {
		parts.pop();
		parts.push('models');
		var data = mapResolver.resolveItem(state, parts);
		if (data == null || !Array.isArray(data)) {
			data = [];
			mapResolver.placeItem(state, parts, data);
			data.push(req.body);
		} else {
			var existingItem = _.find(data, function(item) {
				return item.id == req.body.id;
			});

			if (existingItem != null) {
				_.extend(existingItem, req.body);

				var msg = {
					verb: "update", 
					model: existingItem
				};

				if (etag != null)
					msg.etag = etag;

				parts.pop();
				parts.push(req.body.id);
				var topic = parts.join('/');

				bayClient.publish('/' + topic, msg);
			} else {
				data.push(req.body);

				parts.pop();
				bayClient.publish('/' + parts.join(),
				{
					verb: "reset",
					model: mapResolver.resolveItem(state, parts)
				});
			}
		}
	} else {
		mapResolver.placeItem(state, parts, req.body);
	}

	console.log(util.inspect(state, false, null));
	res.send();
}

function del(req, res) {

}