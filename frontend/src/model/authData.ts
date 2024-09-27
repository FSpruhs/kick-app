export interface AuthData {
  token: string;
  refreshToken: string;
  userName: string;
  userId: string;
  roles: string[];
  email: string;
  authenticated: boolean;
}
