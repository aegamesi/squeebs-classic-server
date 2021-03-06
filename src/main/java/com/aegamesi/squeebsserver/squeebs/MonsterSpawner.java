package com.aegamesi.squeebsserver.squeebs;

import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.util.Util;
import com.aegamesi.squeebsserver.messages.MessageOutSpawnMonster;

public class MonsterSpawner {
    public int x;
    public int y;
    public int rm;
    public int t;
    public int preset = 0;

    public transient Database.MonsterInfo info;
    public transient double timer = -1;

    public boolean disabled = false;
    public double base_timer = 1.5;
    public double base_timer_variance = 3.5;
    public double trigger_on_player = -1; // if a player entering the room triggers spawning
    public boolean normal_spawner = true; // spawn rate decreases as monster amount increases
    public boolean only_one = false; // don't schedule a spawn if there are already monsters
    public boolean regular_spawning = true; // whether to regularly schedule spawns
    public double player_freq_factor = 1.0; // how many players each player counts as for purpose of spawning quicker

    public void init() {
        info = Database.monsterInfo.get(t);

        if(preset == 0) {
            base_timer = 1.5;
            base_timer_variance = 3.5;
            trigger_on_player = -1;
            normal_spawner = true;
            only_one = false;
            regular_spawning = true;
        }
        if(preset == 1) {
            // make
            //"trigger_on_player": 0.5, "only_one": true, "regular_spawning": false
            trigger_on_player = 0.5;
            only_one = true;
            regular_spawning = false;
            normal_spawner = false;
        }
        if(preset == 2) {
            // bspwn
            trigger_on_player = 50;
            regular_spawning = false;
            normal_spawner = false;
        }
        if(preset == 3) {
            // cspwn
            trigger_on_player = 10;
            base_timer = 30;
            base_timer_variance = 10;
            normal_spawner = false;
            regular_spawning = true;
        }

        trigger();
    }

    public void playerEntered() {
        int players_in_room = Util.getPlayersInRoom(rm);
        int monsters_in_room = Util.getMonstersInRoom(rm);

        disabled = false;
        if(trigger_on_player > 0.0) {
            if(only_one && monsters_in_room > 0)
                return;
            timer = trigger_on_player;
        }
    }

    public void trigger() {
        int players_in_room = Util.getPlayersInRoom(rm);
        int monsters_in_room = Util.getMonstersInRoom(rm);

        if(regular_spawning) {
            // regular spawning
            timer = (base_timer + (Util.random.nextDouble() * base_timer_variance));
            timer /= (double)Math.max(players_in_room * player_freq_factor, 1);
        }

        if(players_in_room == 0)
            return;

        if(normal_spawner) {
            //  round(random((instance_number(obj_monster)*4)+5)) = 1
            if(Util.random.nextInt(5 + (monsters_in_room * 4)) != 1)
                return;
        }

        if(disabled)
            return;

        spawn();
    }

    public void spawn() {
        Database.Monster monster = new Database.Monster();
        monster.x = x;
        monster.y = y;
        monster.t = t;
        monster.m_hp = (int)(info.hp * (0.9 + Util.random.nextDouble() * 0.2));
        monster.hp = monster.m_hp;
        monster.rm = rm;
        monster.id = Util.findSlot(Main.db.monsters);
        monster.new_x = x;

        // calculate XP
        monster.xp = (int)Math.round(0.2 * Math.pow(monster.hp, 0.9) + 0.2 * Math.pow(info.arm, 0.5) + 0.3 * Math.pow(info.str, 0.7) - 1);
        if(info.boss)
            monster.xp += (int)Math.pow(monster.hp / 25, 1.5);

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
