using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Memora.Migrations
{
    /// <inheritdoc />
    public partial class RenameNombreUsuarioToNombreCompleto : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "NombreUsuario",
                table: "Usuarios",
                newName: "NombreCompleto");

            migrationBuilder.RenameIndex(
                name: "IX_Usuarios_NombreUsuario",
                table: "Usuarios",
                newName: "IX_Usuarios_NombreCompleto");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "NombreCompleto",
                table: "Usuarios",
                newName: "NombreUsuario");

            migrationBuilder.RenameIndex(
                name: "IX_Usuarios_NombreCompleto",
                table: "Usuarios",
                newName: "IX_Usuarios_NombreUsuario");
        }
    }
}
