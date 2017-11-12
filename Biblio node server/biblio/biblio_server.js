var hostname = '0.0.0.0';
var port = 3000;
var http = require('http');
var mysql = require('mysql');
var url = require('url');
var crypto = require('crypto');
var dbutil = require('./lib/biblio_util');
var db = mysql.createConnection({
    host: '127.0.0.1',
    user: 'biblioclient',
    password: 'UM4L6x1D',
    database: 'biblio'
});

// Version 1.0.5

var server = http.createServer(function(request, response) {
    var queryData = url.parse(request.url, true).query;
    var date = new Date();
    var dateString = '' + date.getFullYear() + '-' + date.getMonth() + '-' +
        date.getDate() + ' ' + date.getHours() + ':' + date.getMinutes() +
        ':' + date.getSeconds();
    // console.log(dateString);
    switch (url.parse(request.url).pathname) {
        case '/register':
            console.log('Someone is registering.');
            var hash = crypto.createHash('sha256').update(
                '' + date.getTime() + queryData.nome + queryData.cognome +
                queryData.matricola + queryData.corsodistudi).digest(
                'hex');
            dbutil.register(db, queryData, hash, function(result) {
                if (result) {
                    response.writeHead(200);
                    var json_response = JSON.stringify({
                        "code": 200,
                        "value": {
                            "uuid": hash
                        }
                    });
                    response.end(json_response);
                } else {
                    response.writeHead(400);
                    var json_response = JSON.stringify({
                        "code": 400,
                        "value": {
                            "error": "MySQL Error"
                        }
                    });
                    response.end(json_response);
                }
            });
            break;
        case '/logon':
            console.log('someone logged on');
            dbutil.log(db, queryData, dateString, function(result) {
                if (result) {
                    response.writeHead(200);
                    var json_response = JSON.stringify({
                        "code": 200,
                        "value": {
                            "response": "User logged successfully."
                        }
                    });
                    response.end(json_response);
                } else {
                    response.writeHead(400);
                    var json_response = JSON.stringify({
                        "code": 400,
                        "value": {
                            "error": "MySQL Error"
                        }
                    });
                    response.end(json_response);
                }
            });
            break;
        case '/logoff':
            dbutil.logoff(db, queryData, dateString, function(result) {
                if (result) {
                    response.writeHead(200);
                    var json_response = JSON.stringify({
                        "code": 200,
                        "value": {
                            "response": "User logged off successfully."
                        }
                    });
                    response.end(json_response);
                } else {
                    response.writeHead(400);
                    var json_response = JSON.stringify({
                        "code": 400,
                        "value": {
                            "error": "MySQL Error"
                        }
                    });
                    response.end(json_response);
                }
            });
            break;
        case '/update':
            dbutil.update(db, queryData, function(result) {
                if (result) {
                    response.writeHead(200);
                    var json_response = JSON.stringify({
                        "code": 200,
                        "value": {
                            "response": "User updated successfully."
                        }
                    });
                    response.end(json_response);
                } else {
                    response.writeHead(400);
                    var json_response = JSON.stringify({
                        "code": 400,
                        "value": {
                            "error": "MySQL Error"
                        }
                    });
                    response.end(json_response);

                }
            });
            break;
        case '/courses':
            if (!queryData.nome) {
                console.log('Requesting courses list');
                db.query("SELECT nome FROM Corso;", function(err, results, fields) {
                    console.log('Querying...');
                    if (err) {
                        console.log(err);
                        response.writeHead(400);
                        var json_response = JSON.stringify({
                            "code": 400,
                            "value": {
                                "error": "MySQL Error"
                            }
                        });
                        response.end(json_response);
                    } else {
                        response.writeHead(200);
                        var json_response = JSON.stringify({
                            "code": 200,
                            "value": results
                        });
                        response.end(json_response);
                    }
                });
            } else {
                console.log('Requesting course id');
                db.query("SELECT id FROM Corso WHERE nome=?;", [queryData.nome], function(err, results, fields) {
                    if (err) {
                        console.log(err);
                        response.writeHead(400);
                        var json_response = JSON.stringify({
                            "code": 400,
                            "value": {
                                "error": "MySQL Error"
                            }
                        });
                        response.end(json_response);
                    } else {
                        response.writeHead(200);
                        var json_response = JSON.stringify({
                            "code": 200,
                            "value": results[0]
                        });
                        response.end(json_response);
                    }
                });
            }

            break;
        case '/frequenza':
            if (!queryData.id) {
                db.query("SELECT U.id, B.nome AS biblioteca, F.timeIN, F.timeOUT FROM Frequenza AS F JOIN (Utente AS U, Biblioteca AS B) ON (F.utente = U.id AND F.biblioteca = B.id);",
                    function(err, results, fields) {
                        if (err) {
                            console.log(err);
                            response.writeHead(400);
                            var json_response = JSON.stringify({
                                "code": 400,
                                "value": {
                                    "error": "MySQL Error"
                                }
                            }, null, 2);
                            response.end(json_response);
                        } else {
                            response.writeHead(200);
                            var json_response = JSON.stringify({
                                "code": 200,
                                "value": results
                            }, null, 2);
                            response.end(json_response);
                        }
                    });
            } else {
                db.query("SELECT B.Nome, F.timeIN, F.timeOUT FROM Frequenza AS F JOIN Biblioteca AS B ON F.biblioteca = B.id WHERE F.utente =?;", [queryData.id],
                    function(err, results, fields) {
                        if (err) {
                            console.log(err);
                            response.writeHead(400);
                            var json_response = JSON.stringify({
                                "code": 400,
                                "value": {
                                    "error": "MySQL Error"
                                }
                            }, null, 2);
                            response.end(json_response);
                        } else {
                            response.writeHead(200);
                            var json_response = JSON.stringify({
                                "code": 200,
                                "value": results
                            }, null, 2);
                            response.end(json_response);
                        }

                    });
            }
            break;

        case '/biblioteca':
            if (!queryData.id) {
                db.query("SELECT * FROM Biblioteca;",
                    function(err, results, fields) {
                        if (err) {
                            console.log(err);
                            response.writeHead(400);
                            var json_response = JSON.stringify({
                                "code": 400,
                                "value": {
                                    "error": "MySQL Error"
                                }
                            }, null, 2);
                            response.end(json_response);
                        } else {
                            response.writeHead(200);
                            var json_response = JSON.stringify({
                                "code": 200,
                                "value": results
                            }, null, 2);
                            response.end(json_response);
                        }
                    });
            } else {
                db.query("SELECT * FROM Biblioteca WHERE id=?;", [queryData.id],
                    function(err, results, fields) {
                        if (err) {
                            console.log(err);
                            response.writeHead(400);
                            var json_response = JSON.stringify({
                                "code": 400,
                                "value": {
                                    "error": "MySQL Error"
                                }
                            }, null, 2);
                            response.end(json_response);
                        } else {
                            response.writeHead(200);
                            var json_response = JSON.stringify({
                                "code": 200,
                                "value": results[0]
                            }, null, 2);
                            response.end(json_response);
                        }
                    });
            }
            break;
        case '/utente':
            if (!queryData.id) {
                response.writeHead(400);
                var json_response = JSON.stringify({
                    "code": 400,
                    "value": {
                        "error": "Bad Request"
                    }
                });
                response.end(json_response);
            } else {
                db.query("SELECT * FROM Utente WHERE id=?;", [queryData.id],
                    function(err, result, fields) {
                        if (err) {
                            response.writeHead(400);
                            var json_response = JSON.stringify({
                                "code": 400,
                                "value": {
                                    "error": "MySQL Error"
                                }
                            });
                            response.end(json_response);
                        } else {
                            response.writeHead(200);
                            var json_response = JSON.stringify({
                                "code": 200,
                                "value": results[0]
                            });
                            response.end(json_response);
                        }
                    });
            }
            break;
        default:
            response.writeHead(400);
            var json_response = JSON.stringify({
                "code": 400,
                "value": {
                    "error": "Bad Request"
                }
            });
            response.end(json_response);
            break;

    }
});
server.listen(port, hostname);
console.log('Server running at http://' + hostname + ':' + port + '/');
