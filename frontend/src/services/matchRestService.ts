import apiClient from '@/services/axiosService';

export interface MatchPreview {
  id: string;
  status: string;
  start: string;
}

export type MatchStatus = 'PLANNED' | 'FINISHED' | 'ENTER_RESULT' | 'CANCELLED';

export interface MatchResponse {
  matchId: string;
  groupId: string;
  start: string;
  playground: string;
  maxPlayer: number;
  minPlayer: number;
  status: string;
  acceptedPlayers: string[];
  deregisteredPlayers: string[];
  waitingBenchPlayers: string[];
  teamA: string[];
  teamB: string[];
  result: string | undefined;
}

export async function getMatchePreviews(groupId: string) {
  return await apiClient.get<MatchPreview[]>(`api/v1/match/group/${groupId}`);
}

export async function getMatch(matchId: string) {
  return await apiClient.get<MatchResponse>(`api/v1/match/${matchId}`);
}
