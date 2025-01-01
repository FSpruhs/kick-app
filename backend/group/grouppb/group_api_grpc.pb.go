// Code generated by protoc-gen-go-grpc. DO NOT EDIT.
// versions:
// - protoc-gen-go-grpc v1.5.1
// - protoc             v3.21.12
// source: group_api.proto

package grouppb

import (
	context "context"

	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.64.0 or later.
const _ = grpc.SupportPackageIsVersion9

const (
	GroupService_IsActivePlayer_FullMethodName            = "/grouppb.GroupService/IsActivePlayer"
	GroupService_GetActivePlayersByGroupID_FullMethodName = "/grouppb.GroupService/GetActivePlayersByGroupID"
	GroupService_HasPlayerAdminRole_FullMethodName        = "/grouppb.GroupService/HasPlayerAdminRole"
)

// GroupServiceClient is the client API for GroupService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type GroupServiceClient interface {
	IsActivePlayer(ctx context.Context, in *IsActivePlayerRequest, opts ...grpc.CallOption) (*IsActivePlayerResponse, error)
	GetActivePlayersByGroupID(ctx context.Context, in *GetActivePlayersByGroupIDRequest, opts ...grpc.CallOption) (*GetActivePlayersByGroupIDResponse, error)
	HasPlayerAdminRole(ctx context.Context, in *HasPlayerAdminRoleRequest, opts ...grpc.CallOption) (*HasPlayerAdminRoleResponse, error)
}

type groupServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewGroupServiceClient(cc grpc.ClientConnInterface) GroupServiceClient {
	return &groupServiceClient{cc}
}

func (c *groupServiceClient) IsActivePlayer(ctx context.Context, in *IsActivePlayerRequest, opts ...grpc.CallOption) (*IsActivePlayerResponse, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(IsActivePlayerResponse)
	err := c.cc.Invoke(ctx, GroupService_IsActivePlayer_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *groupServiceClient) GetActivePlayersByGroupID(ctx context.Context, in *GetActivePlayersByGroupIDRequest, opts ...grpc.CallOption) (*GetActivePlayersByGroupIDResponse, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(GetActivePlayersByGroupIDResponse)
	err := c.cc.Invoke(ctx, GroupService_GetActivePlayersByGroupID_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *groupServiceClient) HasPlayerAdminRole(ctx context.Context, in *HasPlayerAdminRoleRequest, opts ...grpc.CallOption) (*HasPlayerAdminRoleResponse, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(HasPlayerAdminRoleResponse)
	err := c.cc.Invoke(ctx, GroupService_HasPlayerAdminRole_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// GroupServiceServer is the server API for GroupService service.
// All implementations must embed UnimplementedGroupServiceServer
// for forward compatibility.
type GroupServiceServer interface {
	IsActivePlayer(context.Context, *IsActivePlayerRequest) (*IsActivePlayerResponse, error)
	GetActivePlayersByGroupID(context.Context, *GetActivePlayersByGroupIDRequest) (*GetActivePlayersByGroupIDResponse, error)
	HasPlayerAdminRole(context.Context, *HasPlayerAdminRoleRequest) (*HasPlayerAdminRoleResponse, error)
	mustEmbedUnimplementedGroupServiceServer()
}

// UnimplementedGroupServiceServer must be embedded to have
// forward compatible implementations.
//
// NOTE: this should be embedded by value instead of pointer to avoid a nil
// pointer dereference when methods are called.
type UnimplementedGroupServiceServer struct{}

func (UnimplementedGroupServiceServer) IsActivePlayer(context.Context, *IsActivePlayerRequest) (*IsActivePlayerResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method IsActivePlayer not implemented")
}
func (UnimplementedGroupServiceServer) GetActivePlayersByGroupID(context.Context, *GetActivePlayersByGroupIDRequest) (*GetActivePlayersByGroupIDResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method GetActivePlayersByGroupID not implemented")
}
func (UnimplementedGroupServiceServer) HasPlayerAdminRole(context.Context, *HasPlayerAdminRoleRequest) (*HasPlayerAdminRoleResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method HasPlayerAdminRole not implemented")
}
func (UnimplementedGroupServiceServer) mustEmbedUnimplementedGroupServiceServer() {}
func (UnimplementedGroupServiceServer) testEmbeddedByValue()                      {}

// UnsafeGroupServiceServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to GroupServiceServer will
// result in compilation errors.
type UnsafeGroupServiceServer interface {
	mustEmbedUnimplementedGroupServiceServer()
}

func RegisterGroupServiceServer(s grpc.ServiceRegistrar, srv GroupServiceServer) {
	// If the following call pancis, it indicates UnimplementedGroupServiceServer was
	// embedded by pointer and is nil.  This will cause panics if an
	// unimplemented method is ever invoked, so we test this at initialization
	// time to prevent it from happening at runtime later due to I/O.
	if t, ok := srv.(interface{ testEmbeddedByValue() }); ok {
		t.testEmbeddedByValue()
	}
	s.RegisterService(&GroupService_ServiceDesc, srv)
}

func _GroupService_IsActivePlayer_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(IsActivePlayerRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(GroupServiceServer).IsActivePlayer(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: GroupService_IsActivePlayer_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(GroupServiceServer).IsActivePlayer(ctx, req.(*IsActivePlayerRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _GroupService_GetActivePlayersByGroupID_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(GetActivePlayersByGroupIDRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(GroupServiceServer).GetActivePlayersByGroupID(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: GroupService_GetActivePlayersByGroupID_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(GroupServiceServer).GetActivePlayersByGroupID(ctx, req.(*GetActivePlayersByGroupIDRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _GroupService_HasPlayerAdminRole_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(HasPlayerAdminRoleRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(GroupServiceServer).HasPlayerAdminRole(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: GroupService_HasPlayerAdminRole_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(GroupServiceServer).HasPlayerAdminRole(ctx, req.(*HasPlayerAdminRoleRequest))
	}
	return interceptor(ctx, in, info, handler)
}

// GroupService_ServiceDesc is the grpc.ServiceDesc for GroupService service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var GroupService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "grouppb.GroupService",
	HandlerType: (*GroupServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "IsActivePlayer",
			Handler:    _GroupService_IsActivePlayer_Handler,
		},
		{
			MethodName: "GetActivePlayersByGroupID",
			Handler:    _GroupService_GetActivePlayersByGroupID_Handler,
		},
		{
			MethodName: "HasPlayerAdminRole",
			Handler:    _GroupService_HasPlayerAdminRole_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "group_api.proto",
}