# A mock database for testing
from models import Profile
import pickledb
import json

profiles = [None, None]
profiles[0] = Profile(firstname="Kevin",
                  lastname="Lin",
                  image="https://scontent-sjc.xx.fbcdn.net/hphotos-xfp1/v/l/t1.0-9/10653355_10205014838109166_5841389645200777638_n.jpg?oh=ae44ad88ce8cfbe460e4ec6fd4519d49&oe=5589699C",
                  real_email="kaikai526@gmail.com",
                  username="kklin",
                  matches=[]) # this should really be matched with profiles[1]
profiles[1] = Profile(firstname="Gordon",
                  lastname="Lai",
                  image="https://fbcdn-sphotos-f-a.akamaihd.net/hphotos-ak-xpf1/v/t1.0-9/10570353_10203107682745843_148973457800377648_n.jpg?oh=8c73d2139b21760b27cb25d2f2ece957&oe=55846B8B&__gda__=1434804916_5242d884e9c85256e68ec2596ec1c260",
                  real_email="gordonmslai@gmail.com",
                  username="gordonmslai",
                  matches=["kklin"])

db = pickledb.load('C:\Users\Gordon Lai\Documents\wemet\wemet\wemet.db', False)
# db.lcreate("usernames")
# db.ladd("usernames", "gordonmslai")
# db.ladd("usernames", "kklin")
# db.ladd("usernames", "ORbgzHBz")
# db.ladd("usernames", "sIwHAGEV")
# db.ladd("usernames", "NcYagmbN")


def set(key, value):
    db.set(key, value.to_json())
    db.set(key, value)
    db.dump()

def get(key):
    return db.get(key)

def set_profile(username, profile):
    print(profile.to_json())
    print(json.dumps(profile.to_json()))
    db.set(username, json.dumps(profile.to_json()))
    if not username in db.lgetall("usernames"): 
      db.ladd("usernames", username)
    db.dump()

def rem_profile(username):
    db.rem(username)
    lst  = db.lgetall("usernames")
    k = lst.index(username)
    db.lpop("usernames", k)
    db.dump()

def get_profile(username):
    # this is so so wrong
    # db = pickledb.load('/home/kklin/wemet.db', False)
    print(json.loads(db.get(username)))
    return Profile.from_json(json.loads(db.get(username)))

def all_profiles():
    name_list = db.lgetall("usernames")
    py_obj_list = []
    for p in name_list:
      py_obj_list.append(Profile.from_json(json.loads(db.get(p))).to_json())
    return py_obj_list

def add_match(username1, username2):
    u1 = get_profile(username1)
    u2 = get_profile(username2)
    if username2 in u1.matches or username1 == username2:
        return
    u1.matches.append(username2)
    u2.matches.append(username1)
    set_profile(username1, u1)
    set_profile(username2, u2)

def rem_match(username1, username2):
    u1 = get_profile(username1)
    u2 = get_profile(username2)
    if not username2 in u1.matches or username1 == username2:
        return
    u1.matches.remove(username2)
    u2.matches.remove(username1)
    set_profile(username1, u1)
    set_profile(username2, u2)

for name in db.lgetall("usernames"):
  add_match("gordonmslai", name)
  # rem_match("gordonmslai", name)