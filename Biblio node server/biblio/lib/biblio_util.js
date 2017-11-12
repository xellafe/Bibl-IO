module.exports.register = function(db, queryData, hash, result) {
    console.log('registering');

    // console.log(''+hash);
    /*
     * console.log(''+queryData.nome); console.log(''+queryData.cognome);
     * console.log(''+queryData.matricola);
     * console.log(''+queryData.corsodistudi);
     */
    db.query(
        "INSERT INTO Utente (id, nome, cognome) VALUES (?, ?, ?)", [hash, queryData.nome, queryData.cognome],
        function(err) {
            if (err) {
                console.log(err);
                result(false);

            } else {
                result(true);
            }
        });

};

module.exports.log = function(db, queryData, date, cb) {
    db.query(
        "SELECT EXISTS (SELECT Utente.id from Utente where id=?);", [queryData.id],
        function(err, result, fields) {
            if (err || !result) {
                console.log(err);
                cb(false);

            } else {
                console.log('auth ok');
                db.query(
                    "INSERT INTO Frequenza (biblioteca, utente, timeIN) VALUES (?, ?, ?);", [queryData.biblioteca,
                        queryData.id, date
                    ],
                    function(err) {
                        if (err) {
                            console.log(err);
                            cb(false);
                        } else {
                            cb(true);
                        }
                    });

            }

        });
};

module.exports.logoff = function(db, queryData, date, cb) {
    db.query(
        "SELECT EXISTS (SELECT Utente.id from Utente where id=?);", [queryData.id],
        function(err, result, fields) {
            if (err || !result) {
                console.log(err);
                cb(false);
            } else {
                console.log('auth ok');
                db.query(
                    "UPDATE Frequenza SET timeOUT =? WHERE utente =? ORDER BY timeIN DESC LIMIT 1;", [date, queryData.id],
                    function(err) {
                        if (err) {
                            console.log(err);
                            cb(false);
                        } else {
                            cb(true);
                        }
                    });

            }

        });
};

module.exports.update = function(db, queryData, cb) {
    db.query("SELECT EXISTS(SELECT Utente.id from Utente where id=?);", [queryData.id],
        function(err, result, fields) {
            if (err || !result) {
                console.error(err);
                cb(false);
            } else {
                console.log('User found. Now updating...');
                db.query("UPDATE Utente SET Utente.nome=?, Utente.cognome=?, Utente.matricola=?, Utente.corsodistudi=? WHERE Utente.id=?;", [queryData.nome, queryData.cognome, queryData.matricola, queryData.corsodistudi, queryData.id],
                    function(err) {
                        if (err) {
                            console.log(err);
                            cb(false);
                        } else {
                            cb(true);
                        }
                    }
                );
            }
        });
};
