const express = require('express');
const mysql = require('mysql2');
const app = express();
const router = require('router');
const fs = require('fs');
const port = process.env.PORT || 80;

const connection = mysql.createConnection({
    host : 'localhost',
    user : '##########',
    password : '############',
    database : 'week2'
});

app.use(express.json());
app.use(express.urlencoded({extended: true}));

connection.connect();

/*connection.query('select * from levels order by id desc limit 10', (error, rows, fields) => {
    if (error) throw error;
    console.log(rows);
})*/

app.get('/', (req, res)=>{
    res.json({
        success: true
    });
});

// GET /levels/id=N
// id가 N인 레벨을 불러온다.
// 1. 쿼리문 (select * from levels where id=id)를 돌린다.
// 2. 쿼리문의 결과: id가 매칭되는 레벨이 있다! -> leveldata/id.json 파일을 클라이언트에게 전송
// 3. 그게 아니면 그냥 404
/*
app.get('/levels/:id', (req,res)=>{
    var id = req.params.id
    connection.query('select * from levels where id=?', [id], (error, rows, fields)=>{
        res.send(rows);
    });
});
*/
/*connection.query('select * from levels where id=?', [id], (error, rows, fields)=>{
        res.send(rows);
    });
*/
app.get('/levels/:id', (req,res)=>{
    var id = req.params.id
    fs.readFile('/root/leveldata/'+id+'.json', function (err, data){
        if (err) {
            //res.writeHead(404);
            res.status(404).send('not found');
            console.log("get level failed");
        }
        else {
            console.log("get level success")
            var leveldata = JSON.parse(data);
            res.send(leveldata);
        }
    });
});

// POST /levels/
// 레벨을 업로드한다. (POST 시 json 형식으로 자료 전달할 예정!)
// TODO: SQL서버에서 ID를 할당하고, 그 엔트리를 수정하자.

app.post('/levels', (req, res) => {
    var id = req.body.id
    var levelname = req.body.levelname;
    var boardsize = req.body.boardsize;
    console.log(id, levelname, boardsize);
    connection.query('insert into levels(id, levelname, boardsize) values (?, ?, ?)', [id, levelname, boardsize], (error, rows, fields) => 
    {
        console.log(`POST /levels   ID = ${id}`);
        res.send("success");
    });
});

// GET /level_list&page=N
// N ~ N + 10번째 레벨을 불러온다.
// SQL 쿼리문: select * from levels order by id desc limit 10 offset (N * 10)

app.get('/level_list/:page', (req, res)=>{
    var page = req.params.page
    page *=10
    connection.query('select * from levels order by id desc limit 10 offset ?', [page], (error, rows, fields) =>{
        console.log(`GET /level_list/${page}`)
        res.send(rows);
    });
});

//level list
//level 값

//router 객체를 app 객체에 등록
app.use('/', router)

app.listen(port, () => {
    console.log('server is listening at localhost:${process.env.PORT}')
})