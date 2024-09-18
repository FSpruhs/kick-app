import { apiClient } from '@/services/axiosService';

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
  users: GroupUserResponse[];
  inviteLevel: number;
}

export interface GroupUserResponse {
  id: string;
  name: string;
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

export interface UpdatePlayerRolePayload {
  groupId: string;
  updatedUserId: string;
  updatingUserId: string;
  newRole: string;
}

export async function postGroup(payload: GroupPayload) {
  return await apiClient.post<GroupResponse>('api/v1/group', payload);
}

export async function getGroups(userId: string) {
  return await apiClient.get<GroupResponse[]>(`api/v1/group/user/${userId}`);
}

export async function getGroupDetails(groupId: string) {
  return await apiClient.get<GroupDetailResponse>(`api/v1/group/${groupId}`);
}

export async function inviteUserToGroup(payload: GroupInvitePayload) {
  return await apiClient.post(`api/v1/group/user`, payload);
}

export async function responseToGroupInvitation(payload: ResponseToGroupInvitation) {
  return await apiClient.put(`api/v1/group/user`, payload);
}

export async function updatePlayerRole(payload: UpdatePlayerRolePayload) {
  return await apiClient.put(`api/v1/group/player/role`, payload);
}
