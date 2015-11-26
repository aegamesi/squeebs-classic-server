package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutSpawnMonster;

public class MonsterSpawner {
    public int x;
    public int y;
    public int rm;
    public int t;
    public transient Database.MonsterInfo info;

    public double triggerOnPlayer = -1; // if a player entering the room triggers spawning
    public boolean requirePlayer = true; // won't spawn unless there's a player in the room
    public boolean normalSpawner = true; // spawn rate decreases as monster amount increases
    public boolean onlyOne = true; // don't schedule a spawn if there are already monsters
    public double spawnRate = -1; // the time between spawn attempts

    public MonsterSpawner(int x, int y, int rm, int t) {
        this.x = x;
        this.y = y;
        this.t = t;
        info = Database.monsterInfo[t];
    }

    public void attemptSpawn() {
        if(requirePlayer) {
            if(Util.getPlayersInRoom(rm) == 0)
                return;
        }
        if(normalSpawner) {
            //  round(random((instance_number(obj_monster)*4)+5)) = 1
            int monsters = Util.getMonstersInRoom(rm);
            if(Util.random.nextInt(5 + (monsters * 4)) != 1)
                return;
        }

        spawn();

        // schedule next spawn
    }

    public void spawn() {
        Database.Monster monster = new Database.Monster();
        monster.x = x;
        monster.y = y;
        monster.t = t;
        monster.hp = info.hp;
        monster.m_hp = info.hp;
        monster.xp = info.xp;
        monster.rm = rm;
        monster.id = Util.findSlot(Main.db.monsters);
        monster.new_x = x;
        Main.db.monsters[monster.id] = monster;

        // tell people
        MessageOutSpawnMonster spawn = new MessageOutSpawnMonster();
        spawn.x = monster.x;
        spawn.y = monster.y;
        spawn.t = monster.t;
        spawn.id = monster.id;
        Main.clientHandler.broadcast(spawn, monster.rm, null);
    }
}
