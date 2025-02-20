export interface Group {
  id: string;
  name: string;
  players: Player[];
}

export interface Player {
  id: string;
  name: string;
  role: string;
  status: string;
}
