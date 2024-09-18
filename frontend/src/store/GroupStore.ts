import { defineStore } from 'pinia';
import type { Group } from '@/model/group';
const LOCAL_STORAGE_NAME = 'group';

export const useGroupStore = defineStore('group', {
  state: () => ({
    group: (JSON.parse(localStorage.getItem(LOCAL_STORAGE_NAME) ?? 'null') || null) as Group
  }),
  actions: {
    saveGroup(group: Group) {
      console.log('Saving group', group);
      this.group = group;
      localStorage.setItem(LOCAL_STORAGE_NAME, JSON.stringify(this.group));
    },
    getPlayer(userId: string) {
      return this.group.players.find((player) => player.id === userId);
    },
    getGroup() {
      return this.group;
    }
  }
});
