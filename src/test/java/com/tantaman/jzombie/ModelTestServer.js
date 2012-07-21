var app = require('express').createServer();

app.get('/:id', function(req, res){
  res.send({
  	id: req.params.id,
  	itemOne: "One from",
  	itemTwo: "Node",
  	tc: {
  		one: 1
  	}
  });
});

app.listen(80);