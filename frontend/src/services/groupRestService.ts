import apiClient from '@/services/axiosService';

interface GroupPayload {
  name: string;
  userId: string;
}

export interface GroupResponse {
  id: string;
  name: string;
}

export interface GroupDetailResponse {
  id: string;
  name: string;
  players: GroupUserResponse[];
}

export interface GroupUserResponse {
  id: string;
  nickName: string;
  role: string;
  status: string;
}

export interface GroupInvitePayload {
  groupId: string;
  invitedUserId: string;
  invitingUserId: string;
}

export interface ResponseToGroupInvitation {
  groupId: string;
  userId: string;
  accepted: boolean;
}

export interface UpdatePlayerPayload {
  groupId: string;
  updatedUserId: string;
  updatingUserId: string;
  role: string;
  status: string;
}

export interface UserLeaveGroupPayload {
  groupId: string;
  userId: string;
}

export interface RemovePlayerPayload {
  groupId: string;
  removeUserId: string;
  removingUserId: string;
}

export async function postGroup(payload: GroupPayload) {
  return await apiClient.post<GroupResponse>('api/v1/group', payload);
}

export async function getGroups(userId: string) {
  return await apiClient.get<GroupResponse[]>(`api/v1/group/player/${userId}`);
}

export async function getGroupDetails(groupId: string) {
  return await apiClient.get<GroupDetailResponse>(`api/v1/group/${groupId}`);
}

export async function inviteUserToGroup(payload: GroupInvitePayload) {
  return await apiClient.post(`api/v1/group/${payload.groupId}/invited-users/${payload.invitedUserId}`, payload);
}

export async function responseToGroupInvitation(payload: ResponseToGroupInvitation) {
  return await apiClient.put(`api/v1/group/user`, payload);
}

export async function updatePlayer(payload: UpdatePlayerPayload) {
  return await apiClient.put(`api/v1/group/player`, payload);
}

export async function userLeaveGroup(payload: UserLeaveGroupPayload) {
  return await apiClient.delete(`api/v1/group/${payload.groupId}/user/${payload.userId}`);
}

export async function removePlayer(payload: RemovePlayerPayload) {
  return await apiClient.put(`api/v1/group/player/status`, payload);
}
