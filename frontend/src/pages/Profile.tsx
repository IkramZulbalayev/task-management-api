import Layout from "../components/Layout";
import { Badge, Card } from "../components/ui";
import { useAuth } from "../context/AuthContext";

export default function Profile() {
  const { user, isAdmin } = useAuth();

  return (
    <Layout>
      <h1 className="page-title">Profile</h1>
      <Card className="profile-card">
        <div className="profile-row">
          <span className="profile-label">Name</span>
          <span>
            {user?.firstName} {user?.lastName}
          </span>
        </div>
        <div className="profile-row">
          <span className="profile-label">Email</span>
          <span>{user?.email}</span>
        </div>
        <div className="profile-row">
          <span className="profile-label">Organization</span>
          <span>{user?.organizationName}</span>
        </div>
        <div className="profile-row">
          <span className="profile-label">Role</span>
          <Badge tone={isAdmin ? "accent" : "neutral"}>{isAdmin ? "Admin" : "Member"}</Badge>
        </div>
      </Card>
    </Layout>
  );
}
