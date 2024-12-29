// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.34.2
// 	protoc        v3.21.12
// source: group_api.proto

package grouppb

import (
	reflect "reflect"
	sync "sync"

	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

type IsActivePlayerRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	UserId  string `protobuf:"bytes,1,opt,name=userId,proto3" json:"userId,omitempty"`
	GroupId string `protobuf:"bytes,2,opt,name=groupId,proto3" json:"groupId,omitempty"`
}

func (x *IsActivePlayerRequest) Reset() {
	*x = IsActivePlayerRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_group_api_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *IsActivePlayerRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*IsActivePlayerRequest) ProtoMessage() {}

func (x *IsActivePlayerRequest) ProtoReflect() protoreflect.Message {
	mi := &file_group_api_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use IsActivePlayerRequest.ProtoReflect.Descriptor instead.
func (*IsActivePlayerRequest) Descriptor() ([]byte, []int) {
	return file_group_api_proto_rawDescGZIP(), []int{0}
}

func (x *IsActivePlayerRequest) GetUserId() string {
	if x != nil {
		return x.UserId
	}
	return ""
}

func (x *IsActivePlayerRequest) GetGroupId() string {
	if x != nil {
		return x.GroupId
	}
	return ""
}

type IsActivePlayerResponse struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	IsActive bool `protobuf:"varint,1,opt,name=isActive,proto3" json:"isActive,omitempty"`
}

func (x *IsActivePlayerResponse) Reset() {
	*x = IsActivePlayerResponse{}
	if protoimpl.UnsafeEnabled {
		mi := &file_group_api_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *IsActivePlayerResponse) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*IsActivePlayerResponse) ProtoMessage() {}

func (x *IsActivePlayerResponse) ProtoReflect() protoreflect.Message {
	mi := &file_group_api_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use IsActivePlayerResponse.ProtoReflect.Descriptor instead.
func (*IsActivePlayerResponse) Descriptor() ([]byte, []int) {
	return file_group_api_proto_rawDescGZIP(), []int{1}
}

func (x *IsActivePlayerResponse) GetIsActive() bool {
	if x != nil {
		return x.IsActive
	}
	return false
}

type GetActivePlayersByGroupIDRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	GroupId string `protobuf:"bytes,1,opt,name=groupId,proto3" json:"groupId,omitempty"`
}

func (x *GetActivePlayersByGroupIDRequest) Reset() {
	*x = GetActivePlayersByGroupIDRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_group_api_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *GetActivePlayersByGroupIDRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*GetActivePlayersByGroupIDRequest) ProtoMessage() {}

func (x *GetActivePlayersByGroupIDRequest) ProtoReflect() protoreflect.Message {
	mi := &file_group_api_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use GetActivePlayersByGroupIDRequest.ProtoReflect.Descriptor instead.
func (*GetActivePlayersByGroupIDRequest) Descriptor() ([]byte, []int) {
	return file_group_api_proto_rawDescGZIP(), []int{2}
}

func (x *GetActivePlayersByGroupIDRequest) GetGroupId() string {
	if x != nil {
		return x.GroupId
	}
	return ""
}

type GetActivePlayersByGroupIDResponse struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	UserIds []string `protobuf:"bytes,1,rep,name=userIds,proto3" json:"userIds,omitempty"`
}

func (x *GetActivePlayersByGroupIDResponse) Reset() {
	*x = GetActivePlayersByGroupIDResponse{}
	if protoimpl.UnsafeEnabled {
		mi := &file_group_api_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *GetActivePlayersByGroupIDResponse) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*GetActivePlayersByGroupIDResponse) ProtoMessage() {}

func (x *GetActivePlayersByGroupIDResponse) ProtoReflect() protoreflect.Message {
	mi := &file_group_api_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use GetActivePlayersByGroupIDResponse.ProtoReflect.Descriptor instead.
func (*GetActivePlayersByGroupIDResponse) Descriptor() ([]byte, []int) {
	return file_group_api_proto_rawDescGZIP(), []int{3}
}

func (x *GetActivePlayersByGroupIDResponse) GetUserIds() []string {
	if x != nil {
		return x.UserIds
	}
	return nil
}

var File_group_api_proto protoreflect.FileDescriptor

var file_group_api_proto_rawDesc = []byte{
	0x0a, 0x0f, 0x67, 0x72, 0x6f, 0x75, 0x70, 0x5f, 0x61, 0x70, 0x69, 0x2e, 0x70, 0x72, 0x6f, 0x74,
	0x6f, 0x12, 0x07, 0x67, 0x72, 0x6f, 0x75, 0x70, 0x70, 0x62, 0x22, 0x49, 0x0a, 0x15, 0x49, 0x73,
	0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x52, 0x65, 0x71, 0x75,
	0x65, 0x73, 0x74, 0x12, 0x16, 0x0a, 0x06, 0x75, 0x73, 0x65, 0x72, 0x49, 0x64, 0x18, 0x01, 0x20,
	0x01, 0x28, 0x09, 0x52, 0x06, 0x75, 0x73, 0x65, 0x72, 0x49, 0x64, 0x12, 0x18, 0x0a, 0x07, 0x67,
	0x72, 0x6f, 0x75, 0x70, 0x49, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07, 0x67, 0x72,
	0x6f, 0x75, 0x70, 0x49, 0x64, 0x22, 0x34, 0x0a, 0x16, 0x49, 0x73, 0x41, 0x63, 0x74, 0x69, 0x76,
	0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x52, 0x65, 0x73, 0x70, 0x6f, 0x6e, 0x73, 0x65, 0x12,
	0x1a, 0x0a, 0x08, 0x69, 0x73, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28,
	0x08, 0x52, 0x08, 0x69, 0x73, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x22, 0x3c, 0x0a, 0x20, 0x47,
	0x65, 0x74, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x73, 0x42,
	0x79, 0x47, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x44, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x12,
	0x18, 0x0a, 0x07, 0x67, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09,
	0x52, 0x07, 0x67, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x64, 0x22, 0x3d, 0x0a, 0x21, 0x47, 0x65, 0x74,
	0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x73, 0x42, 0x79, 0x47,
	0x72, 0x6f, 0x75, 0x70, 0x49, 0x44, 0x52, 0x65, 0x73, 0x70, 0x6f, 0x6e, 0x73, 0x65, 0x12, 0x18,
	0x0a, 0x07, 0x75, 0x73, 0x65, 0x72, 0x49, 0x64, 0x73, 0x18, 0x01, 0x20, 0x03, 0x28, 0x09, 0x52,
	0x07, 0x75, 0x73, 0x65, 0x72, 0x49, 0x64, 0x73, 0x32, 0xd5, 0x01, 0x0a, 0x0c, 0x47, 0x72, 0x6f,
	0x75, 0x70, 0x53, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x12, 0x51, 0x0a, 0x0e, 0x49, 0x73, 0x41,
	0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x12, 0x1e, 0x2e, 0x67, 0x72,
	0x6f, 0x75, 0x70, 0x70, 0x62, 0x2e, 0x49, 0x73, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c,
	0x61, 0x79, 0x65, 0x72, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x1a, 0x1f, 0x2e, 0x67, 0x72,
	0x6f, 0x75, 0x70, 0x70, 0x62, 0x2e, 0x49, 0x73, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c,
	0x61, 0x79, 0x65, 0x72, 0x52, 0x65, 0x73, 0x70, 0x6f, 0x6e, 0x73, 0x65, 0x12, 0x72, 0x0a, 0x19,
	0x47, 0x65, 0x74, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x73,
	0x42, 0x79, 0x47, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x44, 0x12, 0x29, 0x2e, 0x67, 0x72, 0x6f, 0x75,
	0x70, 0x70, 0x62, 0x2e, 0x47, 0x65, 0x74, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61,
	0x79, 0x65, 0x72, 0x73, 0x42, 0x79, 0x47, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x44, 0x52, 0x65, 0x71,
	0x75, 0x65, 0x73, 0x74, 0x1a, 0x2a, 0x2e, 0x67, 0x72, 0x6f, 0x75, 0x70, 0x70, 0x62, 0x2e, 0x47,
	0x65, 0x74, 0x41, 0x63, 0x74, 0x69, 0x76, 0x65, 0x50, 0x6c, 0x61, 0x79, 0x65, 0x72, 0x73, 0x42,
	0x79, 0x47, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x44, 0x52, 0x65, 0x73, 0x70, 0x6f, 0x6e, 0x73, 0x65,
	0x42, 0x32, 0x5a, 0x30, 0x67, 0x69, 0x74, 0x68, 0x75, 0x62, 0x2e, 0x63, 0x6f, 0x6d, 0x2f, 0x46,
	0x53, 0x70, 0x72, 0x75, 0x68, 0x73, 0x2f, 0x6b, 0x69, 0x63, 0x6b, 0x2d, 0x61, 0x70, 0x70, 0x2f,
	0x62, 0x61, 0x63, 0x6b, 0x65, 0x6e, 0x64, 0x2f, 0x75, 0x73, 0x65, 0x72, 0x2f, 0x67, 0x72, 0x6f,
	0x75, 0x70, 0x70, 0x62, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_group_api_proto_rawDescOnce sync.Once
	file_group_api_proto_rawDescData = file_group_api_proto_rawDesc
)

func file_group_api_proto_rawDescGZIP() []byte {
	file_group_api_proto_rawDescOnce.Do(func() {
		file_group_api_proto_rawDescData = protoimpl.X.CompressGZIP(file_group_api_proto_rawDescData)
	})
	return file_group_api_proto_rawDescData
}

var file_group_api_proto_msgTypes = make([]protoimpl.MessageInfo, 4)
var file_group_api_proto_goTypes = []any{
	(*IsActivePlayerRequest)(nil),             // 0: grouppb.IsActivePlayerRequest
	(*IsActivePlayerResponse)(nil),            // 1: grouppb.IsActivePlayerResponse
	(*GetActivePlayersByGroupIDRequest)(nil),  // 2: grouppb.GetActivePlayersByGroupIDRequest
	(*GetActivePlayersByGroupIDResponse)(nil), // 3: grouppb.GetActivePlayersByGroupIDResponse
}
var file_group_api_proto_depIdxs = []int32{
	0, // 0: grouppb.GroupService.IsActivePlayer:input_type -> grouppb.IsActivePlayerRequest
	2, // 1: grouppb.GroupService.GetActivePlayersByGroupID:input_type -> grouppb.GetActivePlayersByGroupIDRequest
	1, // 2: grouppb.GroupService.IsActivePlayer:output_type -> grouppb.IsActivePlayerResponse
	3, // 3: grouppb.GroupService.GetActivePlayersByGroupID:output_type -> grouppb.GetActivePlayersByGroupIDResponse
	2, // [2:4] is the sub-list for method output_type
	0, // [0:2] is the sub-list for method input_type
	0, // [0:0] is the sub-list for extension type_name
	0, // [0:0] is the sub-list for extension extendee
	0, // [0:0] is the sub-list for field type_name
}

func init() { file_group_api_proto_init() }
func file_group_api_proto_init() {
	if File_group_api_proto != nil {
		return
	}
	if !protoimpl.UnsafeEnabled {
		file_group_api_proto_msgTypes[0].Exporter = func(v any, i int) any {
			switch v := v.(*IsActivePlayerRequest); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_group_api_proto_msgTypes[1].Exporter = func(v any, i int) any {
			switch v := v.(*IsActivePlayerResponse); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_group_api_proto_msgTypes[2].Exporter = func(v any, i int) any {
			switch v := v.(*GetActivePlayersByGroupIDRequest); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_group_api_proto_msgTypes[3].Exporter = func(v any, i int) any {
			switch v := v.(*GetActivePlayersByGroupIDResponse); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_group_api_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   4,
			NumExtensions: 0,
			NumServices:   1,
		},
		GoTypes:           file_group_api_proto_goTypes,
		DependencyIndexes: file_group_api_proto_depIdxs,
		MessageInfos:      file_group_api_proto_msgTypes,
	}.Build()
	File_group_api_proto = out.File
	file_group_api_proto_rawDesc = nil
	file_group_api_proto_goTypes = nil
	file_group_api_proto_depIdxs = nil
}
