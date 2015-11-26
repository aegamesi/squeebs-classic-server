package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutSpawnMonster;

public class MonsterSpawner {
    public int x;
    public int y;
    public int rm;
    public int t;

    public transient Database.MonsterInfo info;
    public transient double timer = -1;

    public double base_timer;
    public double triggerOnPlayer = -1; // if a player entering the room triggers spawning
    public boolean normalSpawner = true; // spawn rate decreases as monster amount increases
    public boolean onlyOne = false; // don't schedule a spawn if there are already monsters

    public MonsterSpawner(int x, int y, int rm, int t) {
        this.x = x;
        this.y = y;
        this.t = t;
        info = Database.monsterInfo[t];
    }

    public void playerEntered() {
        if(triggerOnPlayer > 0.0) {
            timer = triggerOnPlayer;
        }
    }

    public void trigger() {
        int players_in_room = Util.getPlayersInRoom(rm);
        int monsters_in_room = Util.getMonstersInRoom(rm);
        if(triggerOnPlayer < 0.0) {
            // regular spawning
            timer = base_timer / (double)Math.max(players_in_room, 1);
        }

        if(players_in_room == 0)
            return;

        if(onlyOne && monsters_in_room > 0)
            return;

        if(normalSpawner) {
            //  round(random((instance_number(obj_monster)*4)+5)) = 1
            if(Util.random.nextInt(5 + (monsters_in_room * 4)) != 1)
                return;
        }

        spawn();
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
