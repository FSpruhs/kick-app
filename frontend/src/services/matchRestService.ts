import apiClient from "@/services/axiosService";

export interface MatchPreview {
    id: string;
    status: string,
    start: string
}

export async function getMatchePreviews(groupId: string) {
    return await apiClient.get<MatchPreview[]>(`api/v1/match/group/${groupId}`)
}