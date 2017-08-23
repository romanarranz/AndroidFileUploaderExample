var express = require('express');
var multer = require('multer');
var fs = require('fs');
var path = require('path');
var os = require('os');
var passport = require('passport');
var HttpBasicAuth = require('passport-http').BasicStrategy;
var app = express();

var SERVER_PORT = 3000;

const upload = multer({dest: path.join(__dirname, '.', 'uploads') +'/'});

app.get('/', function(req, res) {
    res.end("Android Upload Service Demo node.js server running!");
});

// handle multipart uploads
app.post('/upload/multipart', upload.array('uploaded_file', 1), function(req, res){
    // Hay archivos para subir
    if(req.files.length > 0){
        console.log(req.files);
    }

    res.send("OK");
});

var server = app.listen(SERVER_PORT, function() {
    console.log("Web server started. Listening on all interfaces on port " + server.address().port);
});
