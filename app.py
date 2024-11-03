from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import random
import redis
import json

# docker run -p 6379:6379 -it --name redis redis/redis-stack:latest

app = FastAPI()
redis_data = redis.Redis(host='localhost', port=6379, db=0, username='username', password='mypassword')


def randomdata():
    shablon = [
        [{'name': 'Жим штанги', 'repeat': f'{random.randint(5, 10)} раз', 'img': 'shtan'},
         {'name': f'Присед {random.choice(["широким", "узким", "средним"])} седом со штанги',
          'repeat': f'{random.randint(5, 10)} раз', 'img': 'shtan'}],
        [{'name': 'Выход силы', 'repeat': f'{random.randint(2, 7)} раз', 'img': 'man'}],
        [{'name': 'НЧК', 'repeat': f'{random.randint(10, 20)} раз', 'img': 'pres'}],
        [{'name': 'Рывок гири', 'repeat': f'{random.randint(10, 20)} раз', 'img': 'gir'}],
        [{'name': 'Жим гири', 'repeat': f'{random.randint(10, 20)} раз', 'img': 'gir'}],
        [{'name': 'Скручивание', 'repeat': f'{random.randint(10, 20)} раз', 'img': 'pres'},
         {'name': 'Велосипед', 'repeat': f'{random.randint(10, 20)} раз', 'img': 'pres'}],
    ]
    data = [random.choice(x) for x in shablon]
    return data


@app.get("/{apiKey}/work/old")
def get_items_old(apiKey: str):
    data = redis_data.get(apiKey)
    if data:
        return JSONResponse(content=json.loads(data))
    raise HTTPException(status_code=404, detail="Item not found")


@app.get("/{apiKey}/work/new")
def get_items_new(apiKey: str):
    redis_data.set(apiKey, json.dumps(randomdata()))
    return JSONResponse(content=json.loads(redis_data.get(apiKey)))


@app.post("/{apiKey}/work/addrandom")
def create_item(apiKey: str):
    data = redis_data.get(apiKey)
    if data:
        element = json.loads(data)
        new_element = element + [randomdata()[random.randint(0, 5)]]
        redis_data.set(apiKey, json.dumps(new_element))
        return JSONResponse(content=new_element), 201
    raise HTTPException(status_code=400, detail="Bad Request")


@app.delete("/{apiKey}/work/{item_id}")
def delete_item(apiKey: str, item_id: int):
    data = redis_data.get(apiKey)
    if data:
        element = json.loads(data)
        if 0 <= item_id < len(element):
            del element[item_id]
            redis_data.set(apiKey, json.dumps(element))
            return JSONResponse(content={"message": "Item deleted"}), 204
    raise HTTPException(status_code=400, detail="Bad Request")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=1445)
