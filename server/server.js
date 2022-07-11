const express = require('express');
const mysql = require('mysql2');
const app = express();
const router = require('router');
const fs = require('fs');
const data = fs.readFileSync('conf.json');
const conf = JSON.parse(data);
const port = process.env.PORT || 80;

// db 정보 노출 방지
const connection = mysql.createConnection({
    host : conf.host,
    user : conf.user,
    password : conf.password,
    database : conf.database
});

app.use(express.json());
app.use(express.urlencoded({extended: true}));

connection.connect();

app.get('/', (req, res)=>{
    res.json({
        success: true
    });
});

// GET /levels/id=N
// id가 N인 레t벨을 불러온다.
// 1. 쿼리문 (select * from levels where id=id)를 돌린다.
// 2. 쿼리문의 결과: id가 매칭되는 레벨이 있다! -> leveldata/id.json 파일을 클라이언트에게 전송
// 3. 그게 아니면 그냥 404

app.get('/levels/:id', (req,res)=>{
    let today = new Date();
    var id = req.params.id
    console.log(today.toLocaleTimeString() + `: GET /levels/${id}`)
    fs.readFile('/root/leveldata/'+id+'.json', function (err, data){
        if (err) {
            res.status(404).send('not found');
            console.log(`  getting level ${id} failedl/`);
        }
        else {
            console.log(`  getting level ${id} succeeded/`)
            var leveldata = JSON.parse(data);
            res.send(leveldata);
        }
    });
});

// POST /levels/
// 레벨을 업로드한다. (POST 시 json 형식으로 자료 전달할 예정!)
// TODO: SQL서버에서 ID를 할당하고, 그 엔트리를 수정하자.

app.post('/levels', (req, res) => {
    let today = new Date();
    let content = JSON.stringify(req.body.data)
    let level_name = req.body.metadata.levelname
    let board_size = req.body.metadata.boardsize
    console.log(today.toLocaleTimeString() + `: POST /level`);
    connection.query('insert into levels(levelname, boardsize) values (?, ?)', [level_name, board_size], (error, rows, fields) => {
        if (error) {
            console.log('post level: query failed');
            res.status(400).send('bad request');
        }
        else {
            connection.query('select id from levels where levelname=? and boardsize=? order by id desc limit 1', [level_name, board_size], (error, rows, fields) => {
                let id = rows[0].id;
                fs.writeFile('/root/leveldata/'+id+'.json', content, function(err) 
                {
                    if (err === null) {
                        console.log(`post level: success. Assigned id = ${id}`);
                        res.send("post success");
                    }
                    else {
                        console.log('post level: failed.');
                        connection.query('delete from levels where id=?', [id])
                        res.status(400).send('bad request');
                    }

                });
            });
        }
    });
});

// GET /rating
// user ratin 정보를 불러온다.
// SQL 쿼리문: select * from levels order by id desc limit 10 offset (N * 10)
app.get('/rating_list', (req, res)=>{
    let today = new Date();
    var page = req.params.page
    page *=10
    console.log(today.toLocaleTimeString() + `: GET /rating`)
    connection.query('select * from users order by rating', (error, rows, fields) => {
        if (error) {
            res.status(400).send('bad request');
            console.log("getting rating list failed");
        }
        else {
            res.send(rows);
        }
    });
});


// GET /level_list&page=N
// N ~ N + 10번째 레벨을 불러온다.
// SQL 쿼리문: select * from levels order by id desc limit 10 offset (N * 10)

app.get('/level_list/:page', (req, res)=>{
    let today = new Date();
    var page = req.params.page
    page *=10
    console.log(today.toLocaleTimeString() + `: GET /level_list/${page}`)
    connection.query('select * from levels order by id desc limit 10 offset ?', [page], (error, rows, fields) => {
        if (error) {
            res.status(400).send('bad request');
            console.log("getting level list failed");
        }
        else {
            res.send(rows);
        }
    });
});


// POST /users
// 유저의 정보 업데이트 (점수, 랭킹 등)

app.post('/users', (req, res) => {
    let today = new Date();
    var id = req.body.id;
    var rating = req.body.rating;
    var username = req.body.username;
    console.log(`${req.body}`)
    connection.query('insert into users(id, rating, username) values (?, ?, ?)', [id, rating, username], (error, rows, fields) => {
        console.log(today.toLocaleTimeString() + `: POST /users ID = ${id}`);
        if (error) {
            switch (error.code) {
                case 'ER_DUP_ENTRY':
                    console.log("Duplicate entry, update instead")
                    connection.query('update users set id=?, rating=?, username=? where id=?',
                        [id, rating, username, id], (error, rows, fields) => {
                            if (error) {
                                console.log("Update failed");
                                res.status(400).send('bad request');
                                console.log("Posting user information failed");
                            }
                            else {
                                console.log("Successfully updated user information.");
                                console.log(`  id=${id}, rating=${rating}, username=${username}`)
                                res.send("Update succeeded");
                            }
                        });
                    break;
                default:
                    console.log(`Other errors: ${error.code}`);
                    console.log(`  Request body was ${req.body}`);
                    res.status(400).send('bad request');
                    console.log("Posting user information failed");
                    break;
            }   
        }
        else {
            res.send(`POST /users with ID = ${id} succeeded`);
            console.log(`  id=${id}, rating=${rating}, username=${username}`)
            console.log("Posting user information succeeded");
        }
    });
});

// TODO : level list
// TODO : level 값

//router 객체를 app 객체에 등록
app.use('/', router)

app.listen(port, () => {
    console.log('server is listening at localhost:${process.env.PORT}')
})